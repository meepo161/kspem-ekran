package ru.avem.ekran.communication.utils

open class ConnectionException(message: String) : Exception(message)

class LogicException(message: String) : ConnectionException(message)
class TransportException(message: String) : ConnectionException(message)

class InvalidCommandException(message: String = "") : Exception(message)

enum class TypeException(val title: String, var message: String = "") {
    NONE("Ошибка №448"),
    CONNECTION_LOGIC("Ошибка связи (логическая)"),
    CONNECTION_TRANSPORT("Ошибка связи (транспортная)"),
    CONNECTION_INOPERATIVE("Ошибка связи (недоступно соединение)"),
    INVALID_COMMAND("Ошибка протокола (недопустимая команда)"),
    TIMEOUT("Ошибка превышания ожидания"),
    CONDITION("Ошибка проверки условия"),
    UNEXPECTED("Ошибка №1337"),
    USER_BREAK("Остановлено пользователем"),
}
