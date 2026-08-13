import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
class A : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            println(this)
        }
    }
}
