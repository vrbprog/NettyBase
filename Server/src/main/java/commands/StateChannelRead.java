package commands;

// Определение состояний канала приема данных
public enum StateChannelRead {
    WAIT_META_DATA, READING_META_DATA, CREATE_FILE, READING_FILE
}
