@DslMarker
annotation class StateMachineDsl

@StateMachineDsl
class Builder {
    private var state: String = ""
    private val effects = mutableListOf<String>()

    fun transition(next: String) {
        state = next
    }

    fun effect(name: String) {
        effects.add(name)
    }

    fun build() = state to effects
}

inline fun stateMachine(block: Builder.() -> Unit): Pair<String, List<String>> {
    return Builder().apply(block).build()
}

// Usage:
val result = stateMachine {
    transition("Active")
    effect("LogUpdate")
}
