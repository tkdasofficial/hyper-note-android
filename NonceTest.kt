import java.security.MessageDigest
import java.util.UUID

fun generateNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
fun main() {
    println(generateNonce())
}
