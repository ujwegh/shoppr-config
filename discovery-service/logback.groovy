import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.filter.ThresholdFilter
import de.siegmar.logbackgelf.GelfEncoder
import de.siegmar.logbackgelf.GelfUdpAppender

def SERVICE_NAME = "DISCOVERY-SERVICE"
def LOG_PATH = "${System.getenv('LOGGING_PATH')}"
def NAMESPACE = "${System.getenv('NAMESPACE_FULL')}"
def FILE_NAME = "spring.log"
def FILE_PATTERN = "${FILE_NAME}.%d{yyyy-MM-dd}.%i.gz"
def ENCODER_PATTERN = "%date{dd/MM/yy HH:mm:ss, Europe/Moscow} %level %logger{10} %5p [serviceName=${SERVICE_NAME}, traceId=%X{traceId:-}, spanid=%X{spanId:-}, spanExport=%X{spanExport:-}] [%file:%line] %msg%n %ex{5}"
def ENCODER_PATTERN_GRAYLOG = "%m%n %ex{5}"
def GRAYLOG_SERVER = "192.168.1.66"
def GRAYLOG_PORT = "12201"
def MAX_FILE_SIZE = "1Gb"
def MAX_TOTAL_FILE_SIZE = "4Gb"
int MAX_HISTORY = 2

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "${ENCODER_PATTERN}"
    }
}

appender("GRAYLOG_GELF", GelfUdpAppender) {
    graylogHost = "${GRAYLOG_SERVER}"
    graylogPort = "${GRAYLOG_PORT}" as int
    encoder(GelfEncoder) {
        staticField = "service-name:${SERVICE_NAME}"
        staticField = "namespace:${NAMESPACE}"
        fullPatternLayout(PatternLayout){
            pattern = "${ENCODER_PATTERN_GRAYLOG}"
        }
    }
}

appender("FILE", RollingFileAppender) {
    file ="${LOG_PATH}/${FILE_NAME}"
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_PATH}/${FILE_PATTERN}"
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = MAX_FILE_SIZE
        }
        maxHistory = MAX_HISTORY
        totalSizeCap = MAX_TOTAL_FILE_SIZE
    }
    encoder(PatternLayoutEncoder) {
        pattern = ENCODER_PATTERN
    }
    filter(ThresholdFilter) {
        level = TRACE
    }
}

logger("ru", DEBUG, ["STDOUT", "GRAYLOG_GELF"], false)
root(INFO, ["STDOUT", "FILE"])