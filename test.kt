import java.time.LocalDate
import java.time.ZoneId

fun main() {
    val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    println(startOfDay)
}
