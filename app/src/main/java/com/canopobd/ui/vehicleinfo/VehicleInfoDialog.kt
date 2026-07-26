package com.canopobd.ui.vehicleinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.AstraJ14TurboCalibration
import com.canopobd.R
import com.canopobd.ui.theme.*

@Composable
fun VehicleInfoDialog(
    vin: String,
    onDismiss: () -> Unit
) {
    val cal = AstraJ14TurboCalibration.INSTANCE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.vehicle_info_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                if (vin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = canopoDark
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Pin, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(stringResource(R.string.vehicle_info_vin), fontSize = 10.sp, color = textDim)
                                Text(vin, fontSize = 14.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    item {
                        SectionHeader(stringResource(R.string.vehicle_info_section_motor))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_motor_code), cal.engineCode)
                        InfoRow(stringResource(R.string.vehicle_info_gm_code), cal.gmEngineCode)
                        InfoRow(stringResource(R.string.vehicle_info_valve_config), cal.valveConfig)
                        InfoRow(stringResource(R.string.vehicle_info_displacement), cal.displacement)
                        InfoRow(stringResource(R.string.vehicle_info_bore_stroke), cal.boreStroke)
                        InfoRow(stringResource(R.string.vehicle_info_compression_ratio), cal.compressionRatio)
                        InfoRow(stringResource(R.string.vehicle_info_power), "${cal.maxPowerHp.toInt()} PS / ${cal.maxPowerKw.toInt()} kW @ 4900 ${stringResource(R.string.vehicle_info_rpm_unit)}")
                        InfoRow(stringResource(R.string.vehicle_info_torque), "${cal.maxTorqueNm.toInt()} Nm (${cal.overboostTorqueNm.toInt()} Nm Overboost)")
                        InfoRow(stringResource(R.string.vehicle_info_redline), "${cal.redlineRpm} ${stringResource(R.string.vehicle_info_rpm_unit)}")
                        InfoRow(stringResource(R.string.vehicle_info_idle_rpm), "${cal.idleRpm} ${stringResource(R.string.vehicle_info_rpm_unit)}")
                        InfoRow(stringResource(R.string.vehicle_info_engine_family), cal.engineFamily)
                        InfoRow(stringResource(R.string.vehicle_info_ecu), cal.ecuType)
                        InfoRow(stringResource(R.string.vehicle_info_nox_standard), cal.emissionStandard)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_turbocharger))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_turbocharger_type), cal.turbochargerType)
                        InfoRow(stringResource(R.string.vehicle_info_max_boost), stringResource(R.string.vehicle_info_boost_bar, cal.maxBoostBar))
                        InfoRow(stringResource(R.string.vehicle_info_overboost), "${cal.overboostBar} bar (${cal.overboostTorqueNm.toInt()} Nm)")
                        InfoRow(stringResource(R.string.vehicle_info_boost_control), stringResource(R.string.vehicle_info_wastegate_fixed))
                        InfoRow(stringResource(R.string.vehicle_info_vvt_system), cal.vvtSystem)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_fuel_section))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_fuel_type), stringResource(R.string.vehicle_info_fuel_octane))
                        InfoRow(stringResource(R.string.vehicle_info_min_octane), stringResource(R.string.vehicle_info_min_octane_recommended))
                        InfoRow(stringResource(R.string.vehicle_info_tank_capacity), "${cal.fuelTankLiters.toInt()} L")
                        InfoRow(stringResource(R.string.vehicle_info_fuel_consumption_combined), "${cal.fuelConsumptionCombined} ${stringResource(R.string.vehicle_info_l_per_100km)}")
                        InfoRow(stringResource(R.string.vehicle_info_fuel_consumption_urban), "${cal.fuelConsumptionUrban} ${stringResource(R.string.vehicle_info_l_per_100km)}")
                        InfoRow(stringResource(R.string.vehicle_info_fuel_consumption_extra_urban), "${cal.fuelConsumptionExtraUrban} ${stringResource(R.string.vehicle_info_l_per_100km)}")
                        InfoRow(stringResource(R.string.vehicle_info_co2_emissions), "${cal.co2Emissions} ${stringResource(R.string.vehicle_info_g_per_km)}")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_performance_data))
                    }
                    item {
                        InfoRow(stringResource(R.string.perf_test_0_100), stringResource(R.string.vehicle_info_acceleration_time, cal.accel0to100))
                        InfoRow(stringResource(R.string.vehicle_info_top_speed), "${cal.topSpeed} km/h")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_maintenance_intervals))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_motor_oil), cal.recommendedOil)
                        InfoRow("  ${stringResource(R.string.vehicle_info_alternative)}", cal.alternativeOil)
                        InfoRow("  ${stringResource(R.string.vehicle_info_oil_capacity)}", stringResource(R.string.vehicle_info_oil_capacity_filter, cal.oilCapacityLiters))
                        InfoRow("  ${stringResource(R.string.vehicle_info_interval)}", stringResource(R.string.vehicle_info_interval_km, cal.oilChangeIntervalKm))
                        InfoRow(stringResource(R.string.vehicle_info_air_filter), stringResource(R.string.vehicle_info_interval_km, cal.airFilterIntervalKm))
                        InfoRow(stringResource(R.string.vehicle_info_spark_plugs), "${cal.sparkPlugType}")
                        InfoRow("  ${stringResource(R.string.vehicle_info_spark_plug_gap)}", stringResource(R.string.vehicle_info_spark_plug_gap_mm, cal.sparkPlugGap))
                        InfoRow("  ${stringResource(R.string.vehicle_info_spark_plug_torque)}", cal.sparkPlugTorque)
                        InfoRow("  ${stringResource(R.string.vehicle_info_interval)}", stringResource(R.string.vehicle_info_interval_km, cal.sparkPlugIntervalKm))
                        InfoRow(stringResource(R.string.vehicle_info_coolant), stringResource(R.string.vehicle_info_coolant_dexcool, cal.coolantCapacity))
                        InfoRow("  ${stringResource(R.string.vehicle_info_interval)}", stringResource(R.string.vehicle_info_interval_km, cal.coolantIntervalKm))
                        InfoRow(stringResource(R.string.vehicle_info_timing_chain), stringResource(R.string.vehicle_info_interval_km, cal.timingChainIntervalKm))
                        InfoRow("  ${stringResource(R.string.vehicle_info_note)}", stringResource(R.string.vehicle_info_timing_chain_cold_start_hint))
                        InfoRow(stringResource(R.string.vehicle_info_transmission_fluid), cal.transmissionFluid)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_electric))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_battery), stringResource(R.string.vehicle_info_battery_agm, cal.batteryAh))
                        InfoRow(stringResource(R.string.vehicle_info_alternator), stringResource(R.string.vehicle_info_alternator_format, cal.alternatorV))
                        InfoRow(stringResource(R.string.vehicle_info_charging_voltage), "13.5–14.5V")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(stringResource(R.string.vehicle_info_section_obd2))
                    }
                    item {
                        InfoRow(stringResource(R.string.vehicle_info_protocol), stringResource(R.string.vehicle_info_can_protocol))
                        InfoRow(stringResource(R.string.vehicle_info_connector), stringResource(R.string.vehicle_info_connector_location))
                        InfoRow(stringResource(R.string.vehicle_info_ecu_address), stringResource(R.string.vehicle_info_ecu_address_engine))
                        InfoRow(stringResource(R.string.vehicle_info_extended_pids), stringResource(R.string.vehicle_info_extended_pids_uds))
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = canopoAccent.copy(alpha = 0.15f)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = canopoAccent,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = textSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
