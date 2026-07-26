package com.canopobd.data.protocol

data class UDSResponse(
    val isPositive: Boolean,
    val serviceId: Int,
    val data: ByteArray,
    val errorCode: Int? = null,
    val rawResponse: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UDSResponse
        return isPositive == other.isPositive &&
            serviceId == other.serviceId &&
            data.contentEquals(other.data) &&
            errorCode == other.errorCode &&
            rawResponse == other.rawResponse
    }

    override fun hashCode(): Int {
        var result = isPositive.hashCode()
        result = 31 * result + serviceId
        result = 31 * result + data.contentHashCode()
        result = 31 * result + (errorCode ?: 0)
        result = 31 * result + rawResponse.hashCode()
        return result
    }
}

data class DIDValue(
    val did: String,
    val rawValue: ByteArray,
    val parsedValue: Any? = null,
    val unit: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DIDValue
        return did == other.did && rawValue.contentEquals(other.rawValue)
    }

    override fun hashCode(): Int {
        var result = did.hashCode()
        result = 31 * result + rawValue.contentHashCode()
        return result
    }
}

enum class UDSSessionType(val value: Int, val label: String) {
    DEFAULT(0x01, "Default Session"),
    PROGRAMMING(0x02, "Programming Session"),
    EXTENDED(0x03, "Extended Diagnostic Session"),
    SAFETY_SYSTEM(0x04, "Safety System Session"),
    VEHICLE_MANUFACTURER(0x40, "Vehicle Manufacturer Session"),
    DIAGNOSTIC_SESSION_05(0x05, "Diagnostic Session 05"),
    DIAGNOSTIC_SESSION_06(0x06, "Diagnostic Session 06"),
    DIAGNOSTIC_SESSION_07(0x07, "Diagnostic Session 07")
}

enum class RoutineControlType(val value: Int, val label: String) {
    START(0x01, "Start Routine"),
    STOP(0x02, "Stop Routine"),
    REQUEST_RESULTS(0x03, "Request Routine Results")
}

enum class UDSServiceId(val value: Int, val label: String) {
    DIAGNOSTIC_SESSION_CONTROL(0x10, "DiagnosticSessionControl"),
    ECU_RESET(0x11, "ECUReset"),
    CLEAR_DIAGNOSTIC_INFO(0x14, "ClearDiagnosticInformation"),
    READ_DTC_INFO(0x19, "ReadDTCInformation"),
    READ_DATA_BY_ID(0x22, "ReadDataByIdentifier"),
    READ_MEMORY_BY_ADDRESS(0x23, "ReadMemoryByAddress"),
    SECURITY_ACCESS(0x27, "SecurityAccess"),
    COMMUNICATION_CONTROL(0x28, "CommunicationControl"),
    WRITE_DATA_BY_ID(0x2E, "WriteDataByIdentifier"),
    IO_CONTROL_BY_ID(0x2F, "InputOutputControlByIdentifier"),
    ROUTINE_CONTROL(0x31, "RoutineControl"),
    REQUEST_DOWNLOAD(0x34, "RequestDownload"),
    REQUEST_UPLOAD(0x35, "RequestUpload"),
    TRANSFER_DATA(0x36, "TransferData"),
    REQUEST_TRANSFER_EXIT(0x37, "RequestTransferExit"),
    TESTER_PRESENT(0x3E, "TesterPresent"),
    CONTROL_DTC_SETTING(0x85, "ControlDTCSetting")
}

enum class UDSNegativeResponseCode(val code: Int, val label: String) {
    GENERAL_REJECT(0x10, "General Reject"),
    SERVICE_NOT_SUPPORTED(0x11, "Service Not Supported"),
    SUB_FUNCTION_NOT_SUPPORTED(0x12, "Sub-Function Not Supported"),
    INCORRECT_MESSAGE_LENGTH(0x13, "Incorrect Message Length/Invalid Format"),
    RESPONSE_TOO_LONG(0x14, "Response Too Long"),
    BUSY_REPEAT_REQUEST(0x21, "Busy - Repeat Request"),
    CONDITIONS_NOT_CORRECT(0x22, "Conditions Not Correct"),
    REQUEST_SEQUENCE_ERROR(0x24, "Request Sequence Error"),
    NO_RESPONSE_FROM_SUBNET(0x25, "No Response From Sub-Network Component"),
    FAILURE_PREVENTS_EXECUTION(0x26, "Failure Prevents Execution"),
    REQUEST_OUT_OF_RANGE(0x31, "Request Out Of Range"),
    SECURITY_ACCESS_DENIED(0x33, "Security Access Denied"),
    INVALID_KEY(0x35, "Invalid Key"),
    EXCEED_NUMBER_OF_ATTEMPTS(0x36, "Exceeded Number Of Attempts"),
    REQUIRED_TIME_DELAY_NOT_EXPIRED(0x37, "Required Time Delay Not Expired"),
    UPLOAD_DOWNLOAD_NOT_ACCEPTED(0x70, "Upload/Download Not Accepted"),
    TRANSFER_DATA_SUSPENDED(0x71, "Transfer Data Suspended"),
    GENERAL_PROGRAMMING_FAILURE(0x72, "General Programming Failure"),
    WRONG_BLOCK_SEQUENCE_COUNTER(0x73, "Wrong Block Sequence Counter"),
    REQUEST_CORRECTLY_RECEIVED_PENDING(0x78, "Request Correctly Received - Response Pending"),
    SUB_FUNCTION_NOT_SUPPORTED_IN_SESSION(0x12, "Sub-Function Not Supported In Session"),
    DTC_NOT_READY(0x25, "DTC Not Ready")
}

data class UDSError(
    val serviceId: Int,
    val errorCode: UDSNegativeResponseCode,
    val description: String
) {
    companion object {
        fun fromCode(serviceId: Int, errorCode: Int): UDSError {
            val code = UDSNegativeResponseCode.entries.find { it.code == errorCode }
                ?: UDSNegativeResponseCode.GENERAL_REJECT
            return UDSError(serviceId, code, "${code.label} (0x${errorCode.toString(16).uppercase()})")
        }
    }
}

data class DTCStatus(
    val statusMask: Int,
    val dtcAndSeverityRecord: List<Pair<String, Int>> = emptyList(),
    val funcitonalUnit: Int = 0
)

data class DTCCount(
    val dtcFormatId: Int,
    val dtcCount: Int
)

data class DTCExtendedData(
    val dtc: String,
    val status: Int,
    val occurrenceCount: Int = 0,
    val agingCount: Int = 0,
    val confirmationCount: Int = 0,
    val timestamp: Long = 0L
)
