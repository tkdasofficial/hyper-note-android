import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
fun main() {
    val b = GetSignInWithGoogleOption.Builder("x")
        .setFilterByAuthorizedAccounts(false)
        .setNonce("some_nonce")
        .build()
}
