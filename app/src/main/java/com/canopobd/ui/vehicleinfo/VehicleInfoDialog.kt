package com.canopobd.ui.vehicleinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.AstraJ14TurboCalibration
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
                        text = "Fahrzeugprofil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = textSecondary)
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
                                Text("VIN", fontSize = 10.sp, color = textDim)
                                Text(vin, fontSize = 14.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    item {
                        SectionHeader("Motor")
                    }
                    item {
                        InfoRow("Motorcode", cal.engineCode)
                        InfoRow("GM-Code", cal.gmEngineCode)
                        InfoRow("Bauart", cal.valveConfig)
                        InfoRow("Hubraum", cal.displacement)
                        InfoRow("Bohrung × Hub", cal.boreStroke)
                        InfoRow("Verdichtung", cal.compressionRatio)
                        InfoRow("Leistung", "${cal.maxPowerHp.toInt()} PS / ${cal.maxPowerKw.toInt()} kW @ 4900 U/min")
                        InfoRow("Drehmoment", "${cal.maxTorqueNm.toInt()} Nm (${cal.overboostTorqueNm.toInt()} Nm Overboost)")
                        InfoRow("Redline", "${cal.redlineRpm} U/min")
                        InfoRow("Leerlauf", "${cal.idleRpm} U/min")
                        InfoRow("Motorfamilie", cal.engineFamily)
                        InfoRow("Steuergerät", cal.ecuType)
                        InfoRow("NOx-Norm", cal.emissionStandard)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("Turbolader")
                    }
                    item {
                        InfoRow("Typ", cal.turbochargerType)
                        InfoRow("Ladedruck max", "${cal.maxBoostBar} bar")
                        InfoRow("Overboost", "${cal.overboostBar} bar (${cal.overboostTorqueNm.toInt()} Nm)")
                        InfoRow("Ladedruck-Steuerv.", "Wastegate (festgelegt)")
                        InfoRow("VVT-System", cal.vvtSystem)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("Kraftstoff")
                    }
                    item {
                        InfoRow("Kraftstofftyp", "Benzin")
                        InfoRow("Min. Oktanzahl", "95 RON (98 empfohlen)")
                        InfoRow("Tankinhalt", "${cal.fuelTankLiters.toInt()} L")
                        InfoRow("Verbr. kombiniert", "${cal.fuelConsumptionCombined} L/100km")
                        InfoRow("Verbr. Stadt", "${cal.fuelConsumptionUrban} L/100km")
                        InfoRow("Verbr. Land", "${cal.fuelConsumptionExtraUrban} L/100km")
                        InfoRow("CO2-Emissionen", "${cal.co2Emissions} g/km")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("Leistungsdaten")
                    }
                    item {
                        InfoRow("0–100 km/h", "${cal.accel0to100} s")
                        InfoRow("Höchstgeschw.", "${cal.topSpeed} km/h")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("Wartungsintervalle")
                    }
                    item {
                        InfoRow("Motoröl", cal.recommendedOil)
                        InfoRow("  Alternative", cal.alternativeOil)
                        InfoRow("  Kapazität", "${cal.oilCapacityLiters} L (mit Filter)")
                        InfoRow("  Intervall", "alle ${cal.oilChangeIntervalKm} km")
                        InfoRow("Luftfilter", "alle ${cal.airFilterIntervalKm} km")
                        InfoRow("Zündkerzen", "${cal.sparkPlugType}")
                        InfoRow("  Kerzenstift", "${cal.sparkPlugGap} mm")
                        InfoRow("  Anzugmoment", cal.sparkPlugTorque)
                        InfoRow("  Intervall", "alle ${cal.sparkPlugIntervalKm} km")
                        InfoRow("Kühlmittel", "${cal.coolantCapacity} L (Dex-Cool 50/50)")
                        InfoRow("  Intervall", "alle ${cal.coolantIntervalKm} km")
                        InfoRow("Zahnkette", "alle ${cal.timingChainIntervalKm} km")
                        InfoRow("  Hinweis", "Bei Kaltstart-Rasseln prüfen!")
                        InfoRow("Getriebeöl", cal.transmissionFluid)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("Elektrik")
                    }
                    item {
                        InfoRow("Batterie", "${cal.batteryAh} Ah AGM")
                        InfoRow("Generator", "${cal.alternatorV}V / 120-140A")
                        InfoRow("Ladespannung", "13.5–14.5V")
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("OBD-II Hinweise")
                    }
                    item {
                        InfoRow("Protokoll", "ISO 15765-4 CAN (500 kbit/s)")
                        InfoRow("Stecker", "OBD-II 16-Pin, unter dem Lenkrad links")
                        InfoRow("ECU-Adresse", "0x7E0 (Motor)")
                        InfoRow("Erweiterte PIDs", "Service $22 (UDS)")
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
