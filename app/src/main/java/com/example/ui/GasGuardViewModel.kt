package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Client
import com.example.data.DevicePairingService
import com.example.data.FirebaseAuthService
import com.example.data.GeminiService
import com.example.data.RefillOrder
import com.example.data.SensorReading
import com.example.data.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GasGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(database)

    // --- Authentication & App Mode ---
    private val _currentScreen = MutableStateFlow("SIGN_IN") // SIGN_IN, HOMEOWNER, SUPPLIER
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _signInRole = MutableStateFlow("Homeowner") // "Homeowner" or "Supplier"
    val signInRole: StateFlow<String> = _signInRole.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    // --- Active Tabs ---
    private val _homeownerTab = MutableStateFlow("Home") // Home, Analytics, Refill, Chat
    val homeownerTab: StateFlow<String> = _homeownerTab.asStateFlow()

    private val _supplierTab = MutableStateFlow("Dashboard") // Dashboard, Customers, Dispatch, Chat
    val supplierTab: StateFlow<String> = _supplierTab.asStateFlow()

    private val _supplierDashboardSubTab = MutableStateFlow("Operations") // Operations, Revenue
    val supplierDashboardSubTab: StateFlow<String> = _supplierDashboardSubTab.asStateFlow()

    // --- Database Flow Accessors ---
    val chatMessages: StateFlow<List<com.example.data.ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val refillOrders: StateFlow<List<RefillOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestLeaks: StateFlow<List<com.example.data.LeakRecord>> = repository.latestLeaks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safetyBulletins: StateFlow<List<com.example.data.SafetyBulletin>> = repository.allBulletins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Backend Sync State ---
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow("Idle")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Oct 12, 12:00 PM")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _showSyncSuccessDialog = MutableStateFlow(false)
    val showSyncSuccessDialog: StateFlow<Boolean> = _showSyncSuccessDialog.asStateFlow()

    private val _syncRecordCounts = MutableStateFlow(mapOf("clients" to 0, "orders" to 0, "messages" to 0, "leaks" to 0))
    val syncRecordCounts: StateFlow<Map<String, Int>> = _syncRecordCounts.asStateFlow()

    // --- Onboarding / Safety Check Flow ---
    private val _onboardingActive = MutableStateFlow(false)
    val onboardingActive: StateFlow<Boolean> = _onboardingActive.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1) // Steps 1 to 4
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _calibrationProgress = MutableStateFlow(0f)
    val calibrationProgress: StateFlow<Float> = _calibrationProgress.asStateFlow()

    private val _calibrationReading = MutableStateFlow(0.12)
    val calibrationReading: StateFlow<Double> = _calibrationReading.asStateFlow()

    private val _calibrationStatus = MutableStateFlow("Calibrating...") // "Calibrating...", "Done"
    val calibrationStatus: StateFlow<String> = _calibrationStatus.asStateFlow()

    private val _mq2Mounted = MutableStateFlow(false)
    val mq2Mounted: StateFlow<Boolean> = _mq2Mounted.asStateFlow()

    private val _pressureClampTight = MutableStateFlow(false)
    val pressureClampTight: StateFlow<Boolean> = _pressureClampTight.asStateFlow()

    private val _systemTestReading = MutableStateFlow(0.042)
    val systemTestReading: StateFlow<Double> = _systemTestReading.asStateFlow()

    private val _systemTestPressure = MutableStateFlow(2.15)
    val systemTestPressure: StateFlow<Double> = _systemTestPressure.asStateFlow()

    // --- Homeowner Telemetry State ---
    // Standard 14.2kg LPG cylinder - change this to match your actual tank size.
    private val TANK_CAPACITY_KG = 14.2

    private val _lpgRemaining = MutableStateFlow(0.75) // 75%
    val lpgRemaining: StateFlow<Double> = _lpgRemaining.asStateFlow()

    private val _estimatedDays = MutableStateFlow(12)
    val estimatedDays: StateFlow<Int> = _estimatedDays.asStateFlow()

    private var remainingKg = TANK_CAPACITY_KG * _lpgRemaining.value
    private var avgConsumptionKgPerHour = 0.0
    private var lastConsumptionTimestamp: Long? = null

    private val _avgDailyConsumptionKg = MutableStateFlow(0.0)
    val avgDailyConsumptionKg: StateFlow<Double> = _avgDailyConsumptionKg.asStateFlow()

    // PPM level at/above which we treat the live MQ-2 reading as an active leak. Matches the
    // "< 0.05 ppm" safety threshold referenced elsewhere in the app (chat support, bulletins).
    private val LEAK_THRESHOLD_PPM = 0.05

    private val _currentPpmReading = MutableStateFlow(0.0)
    val currentPpmReading: StateFlow<Double> = _currentPpmReading.asStateFlow()

    private val _isLeakDetected = MutableStateFlow(false)
    val isLeakDetected: StateFlow<Boolean> = _isLeakDetected.asStateFlow()

    // Live MPXV7002DP readings - the differential pressure the sensor measures across the
    // flow restrictor (kPa), and the flow/consumption rate derived from it on the ESP32.
    // Note this is NOT the cylinder's static regulator pressure (that's several bar); it's a
    // small differential pressure used only to estimate flow.
    private val _currentPressureKpa = MutableStateFlow(0.0)
    val currentPressureKpa: StateFlow<Double> = _currentPressureKpa.asStateFlow()

    private val _currentFlowLpm = MutableStateFlow(0.0)
    val currentFlowLpm: StateFlow<Double> = _currentFlowLpm.asStateFlow()

    val consumptionHistory: StateFlow<List<com.example.data.ConsumptionRecord>> = repository.latestConsumption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // "Not configured" (no google-services.json yet), "Signing in...", "Not paired" (no
    // device linked to this account yet), "Connecting...", "Live", or "Offline".
    private val _sensorConnectionStatus = MutableStateFlow("Not configured")
    val sensorConnectionStatus: StateFlow<String> = _sensorConnectionStatus.asStateFlow()

    private val _pairedDeviceId = MutableStateFlow<String?>(null)
    val pairedDeviceId: StateFlow<String?> = _pairedDeviceId.asStateFlow()

    private val _pairingError = MutableStateFlow<String?>(null)
    val pairingError: StateFlow<String?> = _pairingError.asStateFlow()

    private var sensorJob: kotlinx.coroutines.Job? = null
    private var cachedUid: String? = null

    // --- Refill Scheduling Options ---
    private val _selectedDeliveryOption = MutableStateFlow("Standard") // Standard, Express, Emergency
    val selectedDeliveryOption: StateFlow<String> = _selectedDeliveryOption.asStateFlow()

    // --- Tech Support Chat State ---
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _attachedImage = MutableStateFlow<String?>(null) // "sensor_setup_valve_01.jpg"
    val attachedImage: StateFlow<String?> = _attachedImage.asStateFlow()

    private val _isAgentTyping = MutableStateFlow(false)
    val isAgentTyping: StateFlow<Boolean> = _isAgentTyping.asStateFlow()

    // --- Supplier State ---
    private val _dispatchesCount = MutableStateFlow(12)
    val dispatchesCount: StateFlow<Int> = _dispatchesCount.asStateFlow()

    private val _pendingRefillsCount = MutableStateFlow(8)
    val pendingRefillsCount: StateFlow<Int> = _pendingRefillsCount.asStateFlow()

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _customerTypeFilter = MutableStateFlow("ALL") // ALL, RESIDENTIAL, INDUSTRIAL
    val customerTypeFilter: StateFlow<String> = _customerTypeFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.populateDefaultsIfNeeded()
        }
        viewModelScope.launch {
            setUpPairedSensor()
        }
    }

    // Every homeowner's own meter, kept separate from everyone else's: resolves this
    // install's Firebase uid, looks up which deviceId (if any) it has been paired with (see
    // DevicePairingService.kt), and only then starts listening to that specific device's feed.
    private suspend fun setUpPairedSensor() {
        if (!SensorRepository.isSensorConfigured(getApplication())) {
            // No google-services.json yet - nothing to subscribe to. Leave the UI in its
            // clearly-labeled "Not configured" state rather than pretending to have a reading.
            return
        }

        _sensorConnectionStatus.value = "Signing in..."
        val uid = FirebaseAuthService.ensureSignedInUid(getApplication())
        if (uid == null) {
            _sensorConnectionStatus.value = "Offline"
            return
        }
        cachedUid = uid

        val deviceId = DevicePairingService.getPairedDeviceId(uid)
        _pairedDeviceId.value = deviceId
        if (deviceId.isNullOrBlank()) {
            _sensorConnectionStatus.value = "Not paired"
            return
        }

        startObservingDevice(deviceId)
    }

    /** Called from the "Pair your gas meter" UI once the homeowner enters their ESP32's
     * device ID (printed to Serial on first boot - see firmware). */
    fun pairDevice(deviceId: String) {
        val trimmed = deviceId.trim()
        if (trimmed.isEmpty()) {
            _pairingError.value = "Enter your meter's device ID first."
            return
        }
        viewModelScope.launch {
            _pairingError.value = null
            _sensorConnectionStatus.value = "Connecting..."
            val uid = cachedUid ?: FirebaseAuthService.ensureSignedInUid(getApplication())
            if (uid == null) {
                _pairingError.value = "Couldn't sign in - check your google-services.json and that Anonymous sign-in is enabled."
                _sensorConnectionStatus.value = "Not paired"
                return@launch
            }
            cachedUid = uid
            val saved = DevicePairingService.setPairedDeviceId(uid, trimmed)
            if (!saved) {
                _pairingError.value = "Couldn't save pairing - check your connection and try again."
                _sensorConnectionStatus.value = "Not paired"
                return@launch
            }
            _pairedDeviceId.value = trimmed
            startObservingDevice(trimmed)
        }
    }

    /** Unlinks the current meter, e.g. if you're re-pairing to a different device. */
    fun unpairDevice() {
        val uid = cachedUid ?: return
        viewModelScope.launch {
            DevicePairingService.clearPairedDeviceId(uid)
            sensorJob?.cancel()
            _pairedDeviceId.value = null
            _sensorConnectionStatus.value = "Not paired"
            _currentPpmReading.value = 0.0
            _isLeakDetected.value = false
        }
    }

    // Live telemetry from this homeowner's own paired ESP32 (MQ-2 gas sensor + MPXV7002DP
    // pressure/flow sensor), via Cloud Firestore. See SensorRepository.kt for the document
    // this reads from, and /firmware/esp32_mq2_firebase/esp32_mq2_firebase.ino for the
    // device-side code that writes it.
    private fun startObservingDevice(deviceId: String) {
        sensorJob?.cancel()
        _sensorConnectionStatus.value = "Connecting..."

        sensorJob = viewModelScope.launch {
            SensorRepository.observeReadings(deviceId)
                .catch { e ->
                    android.util.Log.e("GasGuardViewModel", "Sensor stream error", e)
                    _sensorConnectionStatus.value = "Offline"
                }
                .collect { reading: SensorReading ->
                    val live = SensorRepository.isLive(reading)
                    _sensorConnectionStatus.value = if (live) "Live" else "Offline"

                    // --- MQ-2 gas leak reading ---
                    val rounded = Math.round(reading.ppm * 1000.0) / 1000.0
                    _currentPpmReading.value = rounded
                    _isLeakDetected.value = live && rounded >= LEAK_THRESHOLD_PPM
                    repository.recordLeakReading(rounded)

                    // --- MPXV7002DP pressure/flow -> consumption tracking ---
                    _currentPressureKpa.value = Math.round(reading.pressureKpa * 1000.0) / 1000.0
                    _currentFlowLpm.value = Math.round(reading.flowLpm * 1000.0) / 1000.0

                    if (live) {
                        val now = reading.timestamp
                        val lastTs = lastConsumptionTimestamp
                        lastConsumptionTimestamp = now
                        if (lastTs != null) {
                            val elapsedMs = (now - lastTs).coerceIn(0, 60_000) // ignore long gaps
                            remainingKg = (remainingKg - reading.consumptionKgPerHour * (elapsedMs / 3_600_000.0))
                                .coerceIn(0.0, TANK_CAPACITY_KG)
                            _lpgRemaining.value = remainingKg / TANK_CAPACITY_KG
                        }
                        // Smooth the rate (EMA) so a single noisy reading doesn't swing the
                        // "estimated days remaining" figure around.
                        avgConsumptionKgPerHour = 0.2 * reading.consumptionKgPerHour + 0.8 * avgConsumptionKgPerHour
                        _avgDailyConsumptionKg.value = avgConsumptionKgPerHour * 24.0
                        if (avgConsumptionKgPerHour > 0.001) {
                            _estimatedDays.value = (remainingKg / (avgConsumptionKgPerHour * 24.0))
                                .toInt().coerceAtLeast(0)
                        }

                        repository.recordConsumption(
                            pressureKpa = _currentPressureKpa.value,
                            flowLpm = _currentFlowLpm.value,
                            consumptionKgPerHour = reading.consumptionKgPerHour
                        )
                    } else {
                        lastConsumptionTimestamp = null
                    }
                }
        }
    }

    // Sign in Actions
    fun updateSignInRole(role: String) {
        _signInRole.value = role
    }

    fun updateEmail(email: String) {
        _emailInput.value = email
    }

    fun updatePassword(password: String) {
        _passwordInput.value = password
    }

    fun performSignIn() {
        val email = _emailInput.value.trim()
        val password = _passwordInput.value

        if (email.isBlank() || password.isBlank()) {
            _signInError.value = "Please enter both an email and a password."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signInError.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _signInError.value = "Password must be at least 6 characters."
            return
        }
        _signInError.value = null

        viewModelScope.launch {
            if (_signInRole.value == "Homeowner") {
                _currentScreen.value = "HOMEOWNER"
                _homeownerTab.value = "Home"
            } else {
                _currentScreen.value = "SUPPLIER"
                _supplierTab.value = "Dashboard"
                _supplierDashboardSubTab.value = "Operations"
            }
        }
    }

    fun signOut() {
        _currentScreen.value = "SIGN_IN"
    }

    // Tab changes
    fun updateHomeownerTab(tab: String) {
        _homeownerTab.value = tab
    }

    fun updateSupplierTab(tab: String) {
        _supplierTab.value = tab
    }

    fun updateSupplierDashboardSubTab(subTab: String) {
        _supplierDashboardSubTab.value = subTab
    }

    // Setup Wizard Actions
    fun startOnboarding() {
        _onboardingActive.value = true
        _onboardingStep.value = 1
        _calibrationProgress.value = 0f
        _calibrationStatus.value = "Calibrating..."
        _calibrationReading.value = 0.12
        _mq2Mounted.value = false
        _pressureClampTight.value = false
        // No fake "searching" step here - Step 1 of the wizard shows this account's real
        // pairing state (pairedDeviceId / sensorConnectionStatus), which setUpPairedSensor()
        // already keeps up to date in the background regardless of whether the wizard is open.
    }

    fun nextOnboardingStep() {
        val nextStep = _onboardingStep.value + 1
        if (nextStep <= 4) {
            _onboardingStep.value = nextStep
            if (nextStep == 2) {
                runCalibrationSimulation()
            }
        }
    }

    fun prevOnboardingStep() {
        val prevStep = _onboardingStep.value - 1
        if (prevStep >= 1) {
            _onboardingStep.value = prevStep
        }
    }

    fun finishOnboarding() {
        _onboardingActive.value = false
        _onboardingStep.value = 1
    }

    private fun runCalibrationSimulation() {
        _calibrationProgress.value = 0f
        _calibrationStatus.value = "Calibrating..."
        _calibrationReading.value = 0.12
        viewModelScope.launch {
            for (i in 1..20) {
                delay(150)
                _calibrationProgress.value = i / 20f
                _calibrationReading.value = (0.12 - (i * 0.004)).coerceAtLeast(0.04)
            }
            _calibrationStatus.value = "Done"
            _calibrationReading.value = 0.038
        }
    }

    fun toggleMq2Mounted(mounted: Boolean) {
        _mq2Mounted.value = mounted
    }

    fun togglePressureClamp(clamp: Boolean) {
        _pressureClampTight.value = clamp
    }

    // Refill ordering Actions
    fun selectDeliveryOption(option: String) {
        _selectedDeliveryOption.value = option
    }

    fun placeRefillOrder() {
        viewModelScope.launch {
            val orderId = "ORDER #VG-${(1000..9999).random()}"
            val volume = 85.2
            val surcharge = when (_selectedDeliveryOption.value) {
                "Express" -> 15.0
                "Emergency" -> 45.0
                else -> 0.0
            }
            val cost = 238.56 + surcharge + 5.0 // includes inspection
            repository.placeRefillOrder(orderId, volume, cost)

            // Tank has been refilled - reset both the displayed % and the underlying kg
            // tracker used for real consumption-based estimates.
            remainingKg = TANK_CAPACITY_KG
            _lpgRemaining.value = 1.0 // Reset to 100%
            _estimatedDays.value = 48 // 48 Days of LPG
            
            // Navigate back to Home
            _homeownerTab.value = "Home"
        }
    }

    // Chat Actions
    fun updateChatInput(text: String) {
        _chatInput.value = text
    }

    fun attachMockImage() {
        _attachedImage.value = "sensor_setup_valve_01.jpg"
    }

    fun removeAttachedImage() {
        _attachedImage.value = null
    }

    fun sendChatMessage() {
        val messageText = _chatInput.value
        val imageUrl = _attachedImage.value
        if (messageText.isBlank() && imageUrl == null) return

        _chatInput.value = ""
        _attachedImage.value = null

        viewModelScope.launch {
            // 1. Save user message to database
            val formattedText = if (imageUrl != null) {
                "[Image attached: $imageUrl]\n$messageText"
            } else {
                messageText
            }
            repository.sendMessage("user", formattedText, imageUrl = imageUrl)

            // 2. Set agent typing state
            _isAgentTyping.value = true
            delay(1500) // Realistic typing delay

            // 3. Request reply from Gemini Service
            val systemInstructions = "You are Alex M., a Technical Support agent for Gas Monitor. You are online, polite, and assist with calibration, sensor mounting (should be within 30cm/12 inches of regulator pointing downwards), and low battery (pulsing orange indicates signal delay or low power)."
            val reply = GeminiService.generateResponse(getApplication(), formattedText, systemInstructions)

            // 4. Save agent reply to database
            repository.sendMessage("agent", reply)
            _isAgentTyping.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Supplier Actions
    fun updateSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun updateTypeFilter(filter: String) {
        _customerTypeFilter.value = filter
    }

    fun scheduleRefillForClient(client: Client) {
        viewModelScope.launch {
            // Update client's fuel level back to 95% and remove refill requested status
            val updatedClient = client.copy(
                fuelLevel = 0.95,
                isRefillRequested = false,
                lastDelivery = "Today"
            )
            repository.insertClient(updatedClient)

            // Record this as a real order in the client's refill history (shown on their
            // customer detail card), not just a fuel-level change.
            val volume = if (client.type == "INDUSTRIAL") 45.0 else 20.0
            val cost = volume * 3.68 // matches the per-gallon rate used elsewhere in the app
            repository.placeRefillOrder(
                orderId = "ORDER #VG-${(1000..9999).random()}",
                volume = volume,
                cost = cost,
                clientId = client.id
            )

            // Increment active dispatches
            _dispatchesCount.value += 1
        }
    }

    fun addNewClient(name: String, address: String, type: String, level: Double, phone: String) {
        viewModelScope.launch {
            val newClient = Client(
                id = UUID.randomUUID().toString().substring(0, 8),
                name = name,
                address = address,
                fuelLevel = level,
                type = type,
                lastDelivery = "Never",
                estRunout = "Unknown",
                isRefillRequested = level < 0.2,
                phone = phone
            )
            repository.insertClient(newClient)
        }
    }

    // --- Backend Sync Action ---
    fun syncWithBackend() {
        viewModelScope.launch {
            // This project ships without a google-services.json, so there is no configured
            // Firebase project yet. Rather than silently fail with a confusing "Sync failed"
            // message every time, tell the user clearly - their local data is never at risk,
            // this feature is entirely optional, and enabling it later is still free (Firebase's
            // Spark plan has a generous free quota with no billing required).
            if (com.google.firebase.FirebaseApp.getApps(getApplication()).isEmpty()) {
                _syncStatus.value = "Cloud sync isn't set up yet (optional - add google-services.json to enable it, still free on Firebase's Spark plan). Your data stays saved locally either way."
                return@launch
            }

            _isSyncing.value = true
            _syncStatus.value = "Signing in..."

            val uid = com.example.data.FirebaseAuthService.ensureSignedInUid(getApplication())
            if (uid == null) {
                _syncStatus.value = "Couldn't authenticate with Firebase - check your google-services.json and that Anonymous sign-in is enabled in the Firebase console."
                _isSyncing.value = false
                return@launch
            }

            _syncStatus.value = "Syncing with cloud..."
            delay(1500) // Aesthetic delay for progress feedback

            // Fetch current state data from lists
            val currentClients = clients.value
            val currentOrders = refillOrders.value
            val currentMessages = chatMessages.value
            val currentLeaks = latestLeaks.value
            val currentConsumption = consumptionHistory.value

            // 1. Sync local data directly into Firebase Firestore, scoped under this uid
            val backupSuccess = com.example.data.GasGuardSyncService.backupData(
                uid = uid,
                clients = currentClients,
                orders = currentOrders,
                messages = currentMessages,
                leaks = currentLeaks,
                consumption = currentConsumption
            )

            // 2. Fetch remote bulletins from Firebase Firestore alerts collection
            val remoteBulletins = com.example.data.GasGuardSyncService.fetchSafetyBulletins()
            if (remoteBulletins.isNotEmpty()) {
                repository.insertBulletins(remoteBulletins)
            }

            if (backupSuccess) {
                _syncStatus.value = "Synced successfully"
                val formatter = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
                _lastSyncTime.value = formatter.format(java.util.Date())
                _syncRecordCounts.value = mapOf(
                    "clients" to currentClients.size,
                    "orders" to currentOrders.size,
                    "messages" to currentMessages.size,
                    "leaks" to currentLeaks.size
                )
                _showSyncSuccessDialog.value = true
            } else {
                _syncStatus.value = "Sync failed"
            }
            _isSyncing.value = false
        }
    }

    fun dismissSyncSuccessDialog() {
        _showSyncSuccessDialog.value = false
    }
}
