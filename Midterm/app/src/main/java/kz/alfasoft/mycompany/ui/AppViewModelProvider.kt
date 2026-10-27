package kz.alfasoft.mycompany.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import kz.alfasoft.mycompany.AppContainer
import kz.alfasoft.mycompany.MyCompanyApp
import kz.alfasoft.mycompany.ui.approvals.ApprovalsViewModel
import kz.alfasoft.mycompany.ui.employee.EmployeeDetailViewModel
import kz.alfasoft.mycompany.ui.home.HomeViewModel
import kz.alfasoft.mycompany.ui.leave.LeaveViewModel
import kz.alfasoft.mycompany.ui.leaveform.LeaveFormViewModel
import kz.alfasoft.mycompany.ui.login.LoginViewModel
import kz.alfasoft.mycompany.ui.org.OrgViewModel
import kz.alfasoft.mycompany.ui.navigation.AppViewModel
import kz.alfasoft.mycompany.ui.profile.ProfileViewModel

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as MyCompanyApp).container

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = container()
            AppViewModel(c.employeeRepository, c.sessionRepository)
        }
        initializer {
            val c = container()
            LoginViewModel(c.employeeRepository, c.sessionRepository)
        }
        initializer {
            val c = container()
            HomeViewModel(c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
        initializer {
            val c = container()
            ProfileViewModel(c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
        initializer {
            val c = container()
            EmployeeDetailViewModel(createSavedStateHandle(), c.employeeRepository, c.leaveRepository, c.clock)
        }
        initializer {
            val c = container()
            LeaveViewModel(c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
        initializer {
            val c = container()
            OrgViewModel(createSavedStateHandle(), c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
        initializer {
            val c = container()
            LeaveFormViewModel(c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
        initializer {
            val c = container()
            ApprovalsViewModel(c.employeeRepository, c.leaveRepository, c.sessionRepository, c.clock)
        }
    }
}
