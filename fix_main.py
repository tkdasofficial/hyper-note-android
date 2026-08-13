with open('app/src/main/java/com/hyper/note/android/MainActivity.kt', 'r') as f:
    content = f.read()

# Add import for AuthScreen
content = content.replace(
    'import com.hyper.note.android.auth.AuthManager',
    'import com.hyper.note.android.auth.AuthManager\nimport com.hyper.note.android.ui.AuthScreen'
)

# Add auth route
composable_auth = """
                        composable("auth") {
                            AuthScreen(
                                authManager = authManager,
                                onAuthSuccess = {
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
"""

content = content.replace(
    'NavHost(navController = navController, startDestination = "home") {',
    'NavHost(navController = navController, startDestination = "home") {' + composable_auth
)

content = content.replace(
    'onAddNote = {',
    'onNavigateToAuth = { navController.navigate("auth") },\n                                onAddNote = {'
)

with open('app/src/main/java/com/hyper/note/android/MainActivity.kt', 'w') as f:
    f.write(content)
