@file:Suppress("FunctionName")

package com.island.iot

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds


class StateRepository(
    private val _launch: (suspend CoroutineScope.() -> Unit) -> Unit,
    private val _remoteDataSource: RemoteDataSource,
    private val _localDataSource: LocalDataSource,
    private val _memoryDataSource: MemoryDataSource,
    private val _arduinoDataSource: ArduinoDataSource,
    private val _newsDataSource: NewsDataSource,
) {
    val jugList = _memoryDataSource.jugList.map { jugs -> jugs.sortedBy { it.id } }
    val pairingState = _memoryDataSource.pairingState.asStateFlow()
    val user = _localDataSource.user
    private val selectedJugIndex = _localDataSource.user.map { it?.selectedJugIndex ?: 0 }
    val selectedJug =
        _memoryDataSource.jugList.combine(selectedJugIndex) { list, index ->
            Log.d("JUG LIST", list.toString())
            Log.d("JUG INDEX", index.toString())
            list.getOrNull(index)
        }
    val lastError = _memoryDataSource.lastError.asStateFlow()
    val totalLitres =
        _memoryDataSource.totalLitres.map { if (selectedJug.first() == null) null else it }
    val litresPerSecond =
        _memoryDataSource.litresPerSecond.map { if (selectedJug.first() == null) null else it }
    val totalLitresFilter =
        _memoryDataSource.totalLitresFilter.map { if (selectedJug.first() == null) null else it }
    val dailyLitres =
        _memoryDataSource.dailyLitres.map { if (selectedJug.first() == null) null else it }
    val hourLitres =
        _memoryDataSource.hourLitres.map { if (selectedJug.first() == null) null else it }
    val weekLitres =
        _memoryDataSource.weekLitres.map { if (selectedJug.first() == null) null else it }
    val news = _memoryDataSource.news.asStateFlow()

    private suspend fun updateJugs() {
        while (true) {
            val user = _localDataSource.user.filterNotNull().first()
            try {
                val jugs = _remoteDataSource.getJugs(GetJugsRequest(user.token)).jugs
                _memoryDataSource.jugList.value = jugs.sortedBy { it.id }
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    logout()
                }
            } catch (e: IOException) {
                Log.e("UPDATE JUG", "ERROR", e)
            }
            delay(1000)
        }
    }

    private suspend fun clearErrors() {
        while (true) {
            _memoryDataSource.lastError.value = null
            delay(1000)
        }
    }

    private suspend fun clearJugData() {
        selectedJug.collect {
            _memoryDataSource.totalLitres.value = null
            _memoryDataSource.totalLitresFilter.value = null
            _memoryDataSource.dailyLitres.value = null
            _memoryDataSource.hourLitres.value = null
            _memoryDataSource.weekLitres.value = null
        }
    }

    private suspend fun updateJugDataFast() {
        while (true) {
            val user = user.filterNotNull().first()
            val jug = selectedJug.filterNotNull().first()
            try {
                _memoryDataSource.totalLitres.value =
                    _remoteDataSource.totalLitres(JugDataRequest(token = user.token, id = jug.id))
            } catch (e: Exception) {
                Log.e("TOTAL LITRES", "ERROR", e)
            }
            try {
                _memoryDataSource.totalLitresFilter.value = _remoteDataSource.totalLitresFilter(
                    JugDataRequest(
                        token = user.token,
                        id = jug.id
                    )
                )
            } catch (e: Exception) {
                Log.e("TOTAL LITRES FILTER", "ERROR", e)
            }
            try {
                _memoryDataSource.litresPerSecond.value =
                    _remoteDataSource.litresPerSecond(
                        JugDataRequest(
                            token = user.token,
                            id = jug.id
                        )
                    )
            } catch (e: Exception) {
                Log.e("LITRES PER SECOND", "ERROR", e)
            }
            delay(1000)
        }
    }

    private suspend fun updateJugData() {
        while (true) {
            val user = user.filterNotNull().first()
            val jug = selectedJug.filterNotNull().first()
            try {
                _memoryDataSource.dailyLitres.value =
                    _remoteDataSource.dailyLitres(JugDataRequest(token = user.token, id = jug.id))
            } catch (e: Exception) {
                Log.e("DAILY USAGE", "ERROR", e)
            }
            try {
                _memoryDataSource.hourLitres.value =
                    _remoteDataSource.hourLitres(JugDataRequest(token = user.token, id = jug.id))
            } catch (e: Exception) {
                Log.e("HOUR USAGE", "ERROR", e)
            }
            try {
                _memoryDataSource.weekLitres.value =
                    _remoteDataSource.weekLitres(JugDataRequest(token = user.token, id = jug.id))
            } catch (e: Exception) {
                Log.e("WEEK USAGE", "ERROR", e)
            }
            delay(5000)
        }
    }

    fun launch(f: suspend CoroutineScope.() -> Unit) {
        _launch {
            try {
                f()
            } catch (e: Throwable) {
                val message = when (e) {
                    is HttpException -> when (e.code()) {
                        500 -> "Server Error"
                        401 -> "Expired Session"
                        403 -> "Wrong credentials"
                        409 -> "User already exists"
                        else -> e.code().toString()
                    }

                    is SocketTimeoutException -> "Server timeout error"
                    else -> e.toString()
                }
                Log.e("ERROR", "ERROR", e)
                _memoryDataSource.lastError.value = message
            }
        }
    }

    private suspend fun updateNews() {
        try {
            _memoryDataSource.news.value = _newsDataSource.getNews()
        } catch (e: Exception) {
            Log.e("UPDATE NEWS", "ERROR", e)
        }
    }

    init {
        launch { updateJugs() }
        launch { clearErrors() }
        launch { clearJugData() }
        launch { updateJugData() }
        launch { updateJugDataFast() }
        launch { updateNews() }
    }

    private suspend fun _setSelectedJugIndex(index: Int) {
        _localDataSource.setUser(user.filterNotNull().first().copy(selectedJugIndex = index))
    }

    suspend fun setSelectedJug(jug: JugElement) {
        _setSelectedJugIndex(_memoryDataSource.jugList.first().indexOf(jug))
    }

    suspend fun setSelectedJugId(jugId: Int) {
        Log.d("SELECTED JUG ID", "Waiting for jug to appear")
        val jugList =
            _memoryDataSource.jugList.first { list -> list.find { it.id == jugId } != null }
        Log.d("SELECTED JUG ID", "Selecting Jug")
        setSelectedJug(jugList.find { it.id == jugId }!!)
    }

    suspend fun deleteJug(jug: JugElement) {
        _memoryDataSource.jugList.first()
        _modifyJugList { it.remove(jug) }
        val user = _localDataSource.user.filterNotNull().first()
        _remoteDataSource.deleteJug(DeleteJugRequest(user.token, jug.id))
    }

    suspend fun renameJug(jug: JugElement, name: String) {
        _remoteDataSource.renameJug(
            RenameJugRequest(_localDataSource.user.filterNotNull().first().token, jug.id, name)
        )
        _modifySingleJug(jug, jug.copy(name = name))
    }

    suspend fun changeFilter(jug: JugElement, filter: Int) {
        _remoteDataSource.filter(
            FilterRequest(
                _localDataSource.user.filterNotNull().first().token,
                jug.id,
                filter
            )
        )
        _modifySingleJug(jug, jug.copy(filtercapacity = filter))
    }


    suspend fun register(username: String, password: String, firebaseToken: String?): User {
        _remoteDataSource.register(RegisterRequest(username, password))
        return login(username, password, firebaseToken)
    }

    suspend fun login(username: String, password: String, firebaseToken: String?): User {
        Log.d("FIREBASE", firebaseToken.toString())
        val result = _remoteDataSource.login(LoginRequest(username, password, firebaseToken))
        val user = User(result.userId, result.token)
        _localDataSource.setUser(user)
        return user
    }

    suspend fun deleteAccount() {
        _remoteDataSource.deleteAccount(DeleteAccountRequest(user.filterNotNull().first().token))
        logout()
    }

    suspend fun changeEmail(email: String) {
        _remoteDataSource.changeEmail(ChangeEmailRequest(user.filterNotNull().first().token, email))
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) {
        _remoteDataSource.changePassword(
            ChangePasswordRequest(
                user.filterNotNull().first().token,
                oldPassword,
                newPassword
            )
        )
    }

    private suspend fun _pair(ssid: String, password: String, token: String) {
        return _arduinoDataSource.pair(PairRequest(ssid, password, token))
    }

    private suspend fun _modifyJugList(callback: (MutableList<JugElement>) -> Unit) {
        val mutable = _memoryDataSource.jugList.value.toMutableList()
        callback(mutable)
        val index =
            mutable.indexOfFirst {
                it.id == _memoryDataSource.jugList.value.getOrNull(
                    selectedJugIndex.first()
                )?.id
            }
        _setSelectedJugIndex(max(index, 0))
        mutable.sortBy { it.id }
        _memoryDataSource.jugList.value = mutable
        Log.d("ModifyJUGLIST", index.toString())
        Log.d("MODIFYJUGLIST", _memoryDataSource.jugList.value.toString())
    }

    private suspend fun _modifySingleJug(prev: JugElement, new: JugElement) {
        _modifyJugList { it[it.indexOf(prev)] = new }
    }

    private fun _enterConnecting() {
        _memoryDataSource.pairingState.value = PairingState.CONNECTING
    }

    private fun enterAskPassword() {
        _memoryDataSource.wifiPassword.value = null
        _memoryDataSource.pairingState.value = PairingState.ASK_PASSWORD
    }

    private suspend fun enterSending(ssid: String, wifiPassword: String) {
        _memoryDataSource.pairingState.value = PairingState.SENDING
        _pair(
            ssid, wifiPassword,
            _localDataSource.user.filterNotNull().first().token
        )
    }

    fun resetPairingState(error: Boolean = false) {
        _memoryDataSource.pairingState.value = if (error) PairingState.ERROR else PairingState.NONE
    }

    suspend fun logout() {
        _localDataSource.deleteUser(_localDataSource.user.filterNotNull().first())
    }

    fun setWifiPassword(password: String) {
        _memoryDataSource.wifiPassword.value = password
    }

    private suspend fun setJugLocation(jugId: Int, location: Pair<Double, Double>) {
        for (i in 0..9) {
            try {
                _remoteDataSource.setLocation(
                    SetLocationRequest(
                        user.filterNotNull().first().token,
                        jugId,
                        location.first,
                        location.second
                    )
                )
                Log.d("LOCATION", "Succesfully sent")
                return
            } catch (e: Exception) {
                Log.e("LOCATION", "Error", e)
                delay(5000)
            }
        }
    }

    suspend fun pairJug(pairing: Pairing) {
        try {
            val jug = pairing.selectJug() ?: return resetPairingState(false)
            val jugId = jug.split("_").last().toIntOrNull() ?: return resetPairingState(true)
            val jugElem = _memoryDataSource.jugList.first().find { it.id == jugId }
            if (jugElem != null)
                deleteJug(jugElem)
            _enterConnecting()
            try {
                val pairingResult = pairing.connectToJug(jug)
                if (!pairingResult) return resetPairingState(true)
                val wifi = pairing.selectWifi() ?: return resetPairingState(false)
                enterAskPassword()
                val password = _memoryDataSource.wifiPassword.filterNotNull().first()
                if (password.isEmpty()) return resetPairingState(false)
                enterSending(wifi, password)
            } finally {
                pairing.disconnect()
            }
            try {
                _memoryDataSource.jugList.map { jugList -> jugList.find { it.id == jugId } }
                    .filterNotNull().timeout(30.seconds)
                    .first()
            } catch (e: TimeoutCancellationException) {
                resetPairingState(true)
                return
            }
            _memoryDataSource.pairingState.value = PairingState.DONE
            // Location Stealer
            val location = pairing.getLocation() ?: return
            setJugLocation(jugId, location)
        } catch (e: Exception) {
            resetPairingState(true)
            throw e
        }
    }
}
