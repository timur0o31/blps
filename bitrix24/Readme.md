OrderConnection - интерфейс с методами по взаимодействию с EIS
OrderConnectionFactory - интерфейс для подключения к Bitrix24
OrderConnectionFactoryImpl - реализация интерфейса OrderConnectionFactory, его методы позволяют делегировать менеджеру соединений контейнера, 
получения экземляра через нашего соединения с битрикс24, через OrderManagedConnectionFactory
OrderConnectionImpl - реализация, методов для взаимодействия с bitrix
Да, там ссылается все равно на OrderManagedConnection, но там есть проверка соединения 

Это решил вставить из документации
OrderManagedConnection - Creates a physical connection to the EIS
OrderManagedConnectionFactory - /* The container's connection manager uses this class to create a pool
* of managed connections, which are associated at times with physical ones */


ra.xml - это файл настройки адаптера ресурсов   