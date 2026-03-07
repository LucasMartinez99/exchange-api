# MSLA - Prueba Técnica Backend Java

## Descripción
Este proyecto implementa un microservicio backend en Java con Spring Boot para realizar conversiones de moneda entre soles peruanos (PEN) y dólares estadounidenses (USD), consumiendo una API externa de tipo de cambio.

Además, el sistema:
- almacena el historial de conversiones en PostgreSQL
- permite consultar el historial por rango de fechas
- permite obtener la suma total convertida por tipo de moneda
- protege los endpoints con autenticación básica
- se encuentra dockerizado
- incluye pruebas unitarias

## Tecnologías utilizadas
- Java 17
- Spring Boot 3.4.4
- Spring Security
- Spring Data JPA
- Spring Cloud OpenFeign
- PostgreSQL
- Docker
- OpenAPI / Swagger
- Maven
- JUnit / Mockito

## Funcionalidades implementadas
1. Conversión de moneda usando una API externa
2. Registro del historial de conversiones en base de datos
3. Consulta de historial por rango de fechas
4. Suma total de montos convertidos por moneda
5. Seguridad con autenticación básica
6. Dockerización de la aplicación y la base de datos
7. Pruebas unitarias del servicio principal

## Estructura del proyecto
El proyecto está organizado en una arquitectura por capas:

- `controller`: expone los endpoints REST
- `service`: contiene la lógica de negocio
- `repository`: acceso a base de datos
- `entity`: entidades JPA
- `dto`: objetos de transferencia de datos
- `mapper`: conversión entre entidades y DTOs
- `feign`: integración con API externa
- `config`: configuración de seguridad
- `exception`: manejo global de errores

## Endpoints principales

### 1. Convertir moneda
**POST** `/api/v1/exchange/convert`

#### Ejemplo de request
```json
{
  "from": "USD",
  "to": "PEN",
  "amount": 10
}