data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double?>,
    val relative_humidity_2m: List<Int?>,
    val wind_speed_10m: List<Double?>
) 