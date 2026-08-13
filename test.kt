import com.google.android.libraries.identity.googleid.GetGoogleIdOption
fun main() {
    val b = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId("x")
}
