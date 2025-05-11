package io.dedyn.engineermantra.botchelper.models
object ClocktowerJson{
    @kotlinx.serialization.Serializable
    data class GameState(
        val bluffs: MutableList<String?>,
        val edition: Edition,
        val roles: String,
        val fabled: MutableList<Fabled>,
        val players: MutableList<Player>
    )
    @kotlinx.serialization.Serializable
    data class Edition(
        val id: String
    )
    @kotlinx.serialization.Serializable
    data class Fabled(
        val id: String
    )
    @kotlinx.serialization.Serializable
    data class Player(
        val name: String,
        val id: String,
        val connected: Boolean,
        val role: Role?, // Can be empty object or string, use sealed class or custom deserializer for advanced use
        val alignmentIndex: Int,
        val reminders: List<Reminder>,
        val isVoteless: Boolean,
        val hasTwoVotes: Boolean,
        val hasResponded: HasResponsedObj?, // Empty object, can be customized based on actual schema
        val isDead: Boolean,
        val pronouns: String
    )
    @kotlinx.serialization.Serializable
    data class Reminder(
        val role: String,
        val name: String
    )
    @kotlinx.serialization.Serializable
    data class Role(
        val id: String
    )
    @kotlinx.serialization.Serializable
    data class HasResponsedObj(
        val hasResponded: Boolean
    )
}

