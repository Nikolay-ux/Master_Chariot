import obd
import logging
from threading import Lock
from typing import List, Dict, Any, Optional

logging.basicConfig(level=logging.INFO)
_logger = logging.getLogger(__name__)

_connection = None
_lock = Lock()

_COMMANDS = {
    "speed": obd.commands.SPEED,                # км/ч
    "rpm": obd.commands.RPM,                    # об/мин
    "coolant_temp": obd.commands.COOLANT_TEMP,  # °C
    "engine_load": obd.commands.ENGINE_LOAD,    # %
    "intake_pressure": obd.commands.INTAKE_PRESSURE,   # кПа
    "maf": obd.commands.MAF,                    # г/с
    "throttle_pos": obd.commands.THROTTLE_POS,  # %
    "fuel_level": obd.commands.FUEL_LEVEL,      # %
    "runtime": obd.commands.RUN_TIME,           # секунды
    "dtc_count": obd.commands.GET_DTC,          # специальная команда для количества и списка DTC
}


def connect(port_str: str = None) -> bool:
    global _connection
    with _lock:
        if _connection and _connection.is_connected():
            _logger.info("Уже подключено.")
            return True
        try:
            if port_str is None:
                _logger.info("Попытка автоматического подключения...")
                _connection = obd.OBD()
            else:
                _logger.info(f"Подключение к порту: {port_str}")
                _connection = obd.OBD(port_str)
            
            if _connection.is_connected():
                _logger.info("Успешно подключено к OBD2!")
                return True
            else:
                _logger.error("Не удалось подключиться к OBD2.")
                return False
        except Exception as e:
            _logger.exception(f"Ошибка при подключении: {e}")
            return False


def disconnect() -> bool:
    global _connection
    with _lock:
        if _connection:
            _connection.close()
            _connection = None
            _logger.info("Соединение закрыто.")
            return True
        return False


def is_connected() -> bool:
    return _connection is not None and _connection.is_connected()


def _query_command(cmd_name: str):
    if not is_connected():
        _logger.warning(f"Нет соединения. Невозможно получить {cmd_name}")
        return None
    cmd = _COMMANDS.get(cmd_name)
    if cmd is None:
        _logger.error(f"Неизвестная команда: {cmd_name}")
        return None
    try:
        response = _connection.query(cmd)
        if response.is_null():
            _logger.debug(f"Нет данных для {cmd_name}")
            return None
        if cmd_name == "runtime":
            val = response.value.magnitude if hasattr(response.value, 'magnitude') else response.value
            return float(val)
        if cmd_name == "dtc_count":
            dtc_list = response.value
            if dtc_list is None:
                return []
            return [str(dtc) for dtc in dtc_list]
        val = response.value
        if hasattr(val, 'magnitude'):
            return val.magnitude
        return val
    except Exception as e:
        _logger.exception(f"Ошибка при запросе {cmd_name}: {e}")
        return None


def get_speed() -> Optional[float]:
    """Скорость автомобиля (км/ч)."""
    return _query_command("speed")


def get_rpm() -> Optional[float]:
    """Обороты двигателя (об/мин)."""
    return _query_command("rpm")


def get_coolant_temp() -> Optional[float]:
    """Температура охлаждающей жидкости (°C)."""
    return _query_command("coolant_temp")


def get_engine_load() -> Optional[float]:
    """Нагрузка двигателя (%)."""
    return _query_command("engine_load")


def get_intake_pressure() -> Optional[float]:
    """Давление во впускном коллекторе (кПа)."""
    return _query_command("intake_pressure")


def get_maf() -> Optional[float]:
    """Расход воздуха (г/с)."""
    return _query_command("maf")


def get_throttle_pos() -> Optional[float]:
    """Положение дроссельной заслонки (%)."""
    return _query_command("throttle_pos")


def get_fuel_level() -> Optional[float]:
    """Уровень топлива (%)."""
    return _query_command("fuel_level")


def get_runtime() -> Optional[float]:
    """Время работы двигателя (секунды)."""
    return _query_command("runtime")


def get_dtc_list() -> Optional[List[str]]:
    """Список кодов неисправностей (DTC)."""
    return _query_command("dtc_count")


def get_all_data() -> Dict[str, Any]:
    """Возвращает словарь со всеми доступными параметрами."""
    data = {}
    for name in _COMMANDS.keys():
        if name == "dtc_count":
            continue
        val = _query_command(name)
        if val is not None:
            data[name] = val
    dtc = get_dtc_list()
    if dtc:
        data["dtc"] = dtc
    return data