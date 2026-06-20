Так как мы должны быть способны реализовывать выполнение нашей программы, через Camunda Cockpit,
а так же через запросы из нашего любимого Postman, мы были должны найти способ, чтобы наша программа могла обращаться 
c Camunda в standalone режиме.

Из статьи мы узнали https://habr.com/ru/articles/510490/, что с Camunda можно обращаться по Rest Api.
Было принято решение, сделать так, чтобы при обращении к контроллеру, контроллер обращался не напрямую к сервису,
а к Camunda, через Rest Api.

Для этого нужно было реализовать правильный вид запроса, чтобы Camunda мог успешно их обрабатывать.

Пример успешного Camunda запроса
curl \
-H "Content-Type: application/json" \
-X POST \
-d '{"variables":{"someData" : {"value" : "someValue", "type": "String"}},"businessKey" : "12345"}}' \
http://localhost:8080/engine-rest/<!-- -->process-definition/key/<!-- -->FlowingTripBookingSaga<!-- -->/start

Для начала нам было необходимо указать адрес нашего Camunda, чтобы мы могли успешно к нему обращаться
camunda.bpm.client.base-url=http://localhost:8080/engine-rest

Для настройки подключения к камунда мы использовали RestClient — это Spring-класс, который позволяет
Java-приложению делать HTTP-запросы к другому сервису.

@Configuration
public class CamundaRestClientConfig {

    @Bean
    public RestClient camundaRestClient(
            @Value("${camunda.bpm.client.base-url}") String camundaRestUrl
    ) {
        return RestClient.builder()
                .baseUrl(camundaRestUrl)
                .build();
    }
}

В последствии будет создан CamundaProcessClient который будет составлять правильный Rest запрос к Camunda
processKey - нужен для нахождения процесса по его ключу
Map<String, CamundaVariable> variables - это набор данных, которые мы передаём внутрь запускаемого BPMN-процесса.
Так как у нас Camunda работает отдельно VariablesInReturn = false -> запустить процесс, переменные обратно не возвращать

CamundaVariable - передаваемые значения

Теперь разберемся с тем, как именно Camunda управляет Spring
Для выполнения этого кода мы использовали ExternalTasks
ExternalTask работает следующим образом, External Task Client внутри Spring регулярно отправляет есть ли задачи с topic save-order,
которые может выполнить blps-worker?
Worker -это исполнитель задач Camunda, в нашем случае Spring
Пока действует блокировка, другой worker не должен выполнить ту же задачу.