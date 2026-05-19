# RunCatching, SupervisorScope & SupervisorJob in Kotlin Coroutines

A practical guide based on real ViewModel usage — what each tool does, when to use them, and how they interact.

---

## Table of Contents

- [RunCatching](#runcatching)
- [SupervisorScope](#supervisorscope)
- [SupervisorJob](#supervisorjob)
- [Combination Rules](#combination-rules)
- [Real World Example](#real-world-example)

---

## RunCatching

### What it is

`runCatching` wraps a block of code and returns a `Result<T>` — either a success or a failure — **without ever throwing an exception**.

```kotlin
val result = runCatching { someRiskyApiCall() }
// Never throws. Always returns Result<T>
```

### Result API

```kotlin
result.isSuccess                        // true if succeeded
result.isFailure                        // true if failed

result.getOrNull()                      // value or null if failed
result.getOrDefault(emptyList())        // value or fallback
result.getOrElse { e -> emptyList() }   // value or handle error inline
result.exceptionOrNull()                // exception or null if success
result.getOrThrow()                     // value or rethrows exception
```

### Without vs With

```kotlin
// Without runCatching
val coins = try {
    useCase.getCoinList()
} catch (e: Exception) {
    null
}

// With runCatching — cleaner
val result = runCatching { useCase.getCoinList() }
val coins = result.getOrNull()
```

### Why it matters in parallel calls

When you have two parallel `async` blocks, a failure in one can cancel the other. `runCatching` prevents this by absorbing the exception:

```kotlin
val coinDeferred = async { runCatching { useCase.getCoinList() } }
val pokemonDeferred = async { runCatching { useCase.getPokemonList() } }

// Both always complete — even if one API fails
val coinResult = coinDeferred.await()    // Result<List<Coin>>
val pokemonResult = pokemonDeferred.await() // Result<List<Pokemon>>
```

### ⚠️ Common mistake — silently hiding errors

```kotlin
// ❌ Wrong — error is swallowed silently
coinList = coinResult.getOrNull()?.map { it.toUI() } ?: emptyList()
// If getCoinList() failed, user sees empty list with no error message

// ✅ Correct — surface the error
coinList = coinResult.getOrNull()?.map { it.toUI() } ?: emptyList(),
coinError = coinResult.exceptionOrNull()?.message ?: "Failed to load coins"
```

---

## SupervisorScope

### What it is

`supervisorScope` creates a coroutine scope where **one child failure does not cancel sibling coroutines**. It has a built-in `SupervisorJob` — you don't need to add one yourself.

```kotlin
supervisorScope {
    val a = async { failingApiCall() }   // throws exception
    val b = async { successApiCall() }   // still runs ✅
}
```

Without `supervisorScope`, a failure in `a` would cancel `b`.

### How exceptions work

Unlike `runCatching`, exceptions inside `supervisorScope` **do still propagate** — just only after all children finish, and only from the one that failed.

```kotlin
supervisorScope {
    val a = async { useCase.getCoinList() }    // can throw
    val b = async { useCase.getPokemonList() } // can throw

    // supervisorScope ensures b runs even if a throws
    val coinResult = runCatching { a.await() }    // catch per call
    val pokemonResult = runCatching { b.await() } // catch per call
}
```

### When do you actually need it?

You need `supervisorScope` when:
- Your `async` blocks can throw (no `runCatching` inside them)
- You want siblings to keep running if one fails

You **don't** need it when:
- You've already used `runCatching` inside each `async` block — nothing can throw, so there's nothing to protect against

---

## SupervisorJob

### What it is

`SupervisorJob` is a `Job` that allows child coroutines to fail independently — a failing child does not cancel its siblings or the parent scope.

```kotlin
// Regular Job — one child fails → all siblings cancelled
val scope = CoroutineScope(Dispatchers.IO)

// SupervisorJob — one child fails → siblings keep running
val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

### viewModelScope already has it

This is the most important thing to know in Android development:

```kotlin
// Jetpack creates viewModelScope like this internally:
val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
```

**You never need to add `SupervisorJob` manually when using `viewModelScope`.** It is already there.

```kotlin
// ❌ Unnecessary
viewModelScope.launch(SupervisorJob()) { ... }

// ✅ Correct — SupervisorJob is already built in
viewModelScope.launch { ... }
```

### When would you need it manually?

Only when creating your **own custom scope** outside of `viewModelScope`:

```kotlin
// ❌ Dangerous — regular Job, one failure cancels everything
class MyRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
}

// ✅ Safe — SupervisorJob protects sibling coroutines
class MyRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
```

---

## Combination Rules

### Rule 1 — `runCatching` inside `async` + `supervisorScope` → ❌ Redundant

```kotlin
// ❌ supervisorScope is useless here
supervisorScope {
    val a = async { runCatching { useCase.getCoinList() } }
    val b = async { runCatching { useCase.getPokemonList() } }
    // runCatching already absorbs all exceptions
    // nothing can throw → supervisorScope has nothing to protect
}
```

`runCatching` absorbs all exceptions inside `async`, so `supervisorScope` never has any failure to handle. It just sits there doing nothing.

---

### Rule 2 — `runCatching` alone → ✅ Clean and sufficient

```kotlin
// ✅ Best for production
val coinDeferred = async { runCatching { useCase.getCoinList() } }
val pokemonDeferred = async { runCatching { useCase.getPokemonList() } }

val coinResult = coinDeferred.await()
val pokemonResult = pokemonDeferred.await()
```

Since `runCatching` absorbs all throws:
- `.await()` never throws
- Both `async` blocks always complete
- No `supervisorScope` needed
- No `try/catch` needed

---

### Rule 3 — `supervisorScope` alone (no `runCatching` inside async) → ✅ Valid

```kotlin
// ✅ Valid — supervisorScope protects siblings from each other
supervisorScope {
    val a = async { useCase.getCoinList() }    // can throw
    val b = async { useCase.getPokemonList() } // can throw

    // catch errors individually on .await()
    val coinResult = runCatching { a.await() }
    val pokemonResult = runCatching { b.await() }
}
```

Here both tools serve **different purposes**:
- `supervisorScope` → prevents one `async` failure from cancelling the other
- `runCatching` on `.await()` → lets you handle each result independently

---

### Rule 4 — `SupervisorJob` + `viewModelScope` → ❌ Redundant

```kotlin
// ❌ SupervisorJob already inside viewModelScope
viewModelScope.launch(SupervisorJob()) { ... }

// ✅ Just use viewModelScope directly
viewModelScope.launch { ... }
```

---

### Summary table

| Combination | Valid? | Reason |
|---|---|---|
| `runCatching` inside `async` alone | ✅ | Nothing can throw, siblings are safe |
| `supervisorScope` alone + `runCatching` on `.await()` | ✅ | Each serves a different purpose |
| `runCatching` inside `async` + `supervisorScope` | ❌ | Redundant — both solve the same problem |
| `runCatching` inside `async` + outer `try/catch` | ❌ | Redundant — nothing escapes `runCatching` |
| Manual `SupervisorJob` + `viewModelScope` | ❌ | `viewModelScope` already has one |
| Manual `SupervisorJob` + custom `CoroutineScope` | ✅ | Necessary when building your own scope |

---

## Real World Example

### The state

```kotlin
data class NewAppScreenState(
    val isLoading: Boolean = false,
    val coinList: List<CoinUI> = emptyList(),
    val pokemonList: List<PokemonUI> = emptyList(),
    val coinError: String? = null,
    val pokemonError: String? = null
)
```

### The ViewModel

```kotlin
@HiltViewModel
class NewAppViewModel @Inject constructor(
    private val useCase: NewAppUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewAppScreenState())
    val state: StateFlow<NewAppScreenState> = _state.asStateFlow()

    fun handleIntent(intent: NewAppScreenIntent) {
        viewModelScope.launch {
            when (intent) {
                is NewAppScreenIntent.LoadAllData -> {

                    _state.value = _state.value.copy(
                        isLoading = true,
                        coinError = null,
                        pokemonError = null
                    )

                    // Parallel API calls — both always complete
                    val coinDeferred = async { runCatching { useCase.getCoinList() } }
                    val pokemonDeferred = async { runCatching { useCase.getPokemonList() } }

                    val coinResult = coinDeferred.await()
                    val pokemonResult = pokemonDeferred.await()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        coinList = coinResult.getOrNull()
                            ?.map { it.toUI() }
                            ?: emptyList(),
                        pokemonList = pokemonResult.getOrNull()
                            ?.map { it.toUI() }
                            ?: emptyList(),
                        coinError = coinResult.exceptionOrNull()?.message
                            ?: "Failed to load coins",
                        pokemonError = pokemonResult.exceptionOrNull()?.message
                            ?: "Failed to load pokemon"
                    )
                }
            }
        }
    }
}
```

### Why this is the best production approach

| Concern | How it's handled |
|---|---|
| Parallel loading | `async` + `await` |
| One API fails, other still loads | `runCatching` inside each `async` |
| Error surfaced to UI | `exceptionOrNull()?.message` |
| Separate error per API | Two error fields in state |
| No unnecessary wrappers | No `supervisorScope`, no `try/catch` |
| SupervisorJob | Already inside `viewModelScope` |

### The UI

```kotlin
when {
    state.isLoading -> {
        CircularProgressIndicator()
    }
    else -> {
        LazyColumn {
            item {
                Text("Pokemon List", fontWeight = FontWeight.Bold)
            }

            // Show pokemon error inline if failed
            if (!state.pokemonError.isNullOrEmpty()) {
                item {
                    Text(
                        text = state.pokemonError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items(state.pokemonList) { pokemon -> PokemonCard(pokemon) }

            item {
                Text("Coin List", fontWeight = FontWeight.Bold)
            }

            // Show coin error inline if failed
            if (!state.coinError.isNullOrEmpty()) {
                item {
                    Text(
                        text = state.coinError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items(state.coinList) { coin -> CoinCard(coin) }
        }
    }
}
```

This way if one API fails — the error shows inline, the other list still displays normally.

---

## Key Takeaways

1. **`runCatching` inside `async`** → nothing can throw, siblings are always safe
2. **`supervisorScope`** → only useful when `async` blocks can throw (no `runCatching` inside them)
3. **`SupervisorJob`** → already built into `viewModelScope`, never add it manually
4. **Never use `runCatching` inside `async` AND `supervisorScope` together** — they solve the same problem
5. **Always surface errors** — `getOrNull()` silently hides failures, use `exceptionOrNull()?.message` too
