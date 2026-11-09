package com.fleettrack.app.ui.manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.fleettrack.app.ui.owner.officeregister.OfficeRegisterScreen
import com.fleettrack.app.ui.owner.officeregister.OfficeRegisterViewModel

@Composable
fun ManagerRegisterScreen(
    onNavigateBack: () -> Unit,
    dashboardViewModel: ManagerDashboardViewModel = hiltViewModel(),
    registerViewModel: OfficeRegisterViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val session by dashboardViewModel.managerSession.collectAsState()

    LaunchedEffect(session.managerId) {
        val id = session.managerId ?: "mgr_moosapet_01"
        registerViewModel.setManagerFilter(id)
    }

    OfficeRegisterScreen(
        viewModel = registerViewModel,
        onBackClick = onNavigateBack,
        modifier = modifier.fillMaxSize()
    )
}

