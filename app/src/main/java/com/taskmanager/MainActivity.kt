package com.taskmanager


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.taskmanager.data.TaskRepository
import com.taskmanager.network.Retrofit
import com.taskmanager.screen.AddEditTaskScreen
import com.taskmanager.screen.TaskDetailScreen
import com.taskmanager.screen.TaskListScreen
import com.taskmanager.ui.theme.TaskManagerTheme
import com.taskmanager.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Keep a local ref to ViewModel if you use by viewModels() DI etc.
    private var viewModelInstance: TaskViewModel? = null
    private val isDarkMode = mutableStateOf(false)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = try {
            task.getResult(Exception::class.java)
        } catch (e: Exception) {
            Log.e("SignIn", "failed to get account", e)
            null
        }

        account?.let { acct ->
            // Launch coroutine to fetch access token (network/IO)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // IMPORTANT: scope must be "oauth2:<scope_url>"
                    val scope = "oauth2:https://www.googleapis.com/auth/tasks"
                    val token = GoogleAuthUtil.getToken(applicationContext, acct.account!!, scope)

                    // create Retrofit + repository + viewmodel (on main thread)
                    launch(Dispatchers.Main) {
                        initAppWithToken(token)
                    }
                } catch (e: Exception) {
                    Log.e("Auth", "token error", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSignIn()
    }

    private fun startSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/tasks"))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun initAppWithToken(token: String) {
        // create retrofit service & repository
        val service = Retrofit.create(token)
        val repo = TaskRepository(service)
        val vm = TaskViewModel(repo)
        viewModelInstance = vm

        enableEdgeToEdge()
        setContent {
            TaskManagerTheme(darkTheme = isDarkMode.value) {
                AppNavHost(vm, isDarkMode)
            }
        }

        // kick off initial load
        vm.loadTasks(true)
    }
}

@Composable
fun AppNavHost(taskViewModel: TaskViewModel, isDarkMode: MutableState<Boolean>) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "taskList"
    ) {
        composable("taskList") {
            TaskListScreen(viewModel = taskViewModel,
                isDarkMode = isDarkMode,
                onAddTaskClick = { navController.navigate("addTask") },
                onTaskClick = { id -> navController.navigate("taskDetail/$id") })
        }

        composable("addTask") {
            AddEditTaskScreen(
                viewModel = taskViewModel, onSave = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }

        composable(
            "taskDetail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(viewModel = taskViewModel, onEdit = { navController.navigate("editTask/${id}") }, onBack = { navController.popBackStack() })
        }

        composable(
            "editTask/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("taskId") ?: ""
            AddEditTaskScreen(
                viewModel = taskViewModel, taskId = id, onSave = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
    }
}