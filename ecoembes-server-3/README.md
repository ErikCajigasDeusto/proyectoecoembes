# 🏗️ Ecoembes Server — Version 2

## English

### Overview

Ecoembes Server V2 is the foundational backend of the *Ecoembes Service* teaching case study, a simplified distributed garbage distribution system for third-year Computer Engineering class pryect. It manages garbage bins, garbage disposal, users and garbage plant capacity through a Spring Boot REST API.

The version is for class assingment. It illustrates the Façade, Application Service, Data Transfer Object and State Management patterns.

### REST API

| Method | Endpoint | Description |
|:--|:--|:--|
| POST | `/auth/login` | Log in and obtain a token |
| POST | `/auth/logout` | Log out using a token |
| POST | `/contenedores` | Create a garbage container (requires login) |
| PUT | `/contenedores/{id}/estado` | Updates a container's filling volume automaticaly |
| GET | `/contenedores/{id}/uso` | Retrieve a container's usage data (requires login) |
| GET | `/contenedores/{id}/estado` | Retrieve a conatiner's filling volume (requires login) |
| GET | `/contenedores/zona` | Retrieve a list of containers from a specific zone (requires login) |
| GET | `/planta/capacidades` | Retrieve all of garbage plants capacities (requires login) |
| POST | `/asignaciones` | Create an allocation between containers and a garbage plant (requires login) |
| GET | `/asignaciones/{id}` | Retrieve an allocation |
| GET | `/asignaciones` | Retrieve all allocations |
| GET | `/alertas/saturacion` | Retrieve the saturation of plants (requires login) |
| POST | `/alertas/saturacion/evaluar` | Scans the filling volume of all containers and creates an alert based of that (requires login) |

- Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- OpenAPI document: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

### Requirements and run on Windows, macOS and Linux

Requires JDK 21.

#### Windows

From PowerShell:

```powershell
.\gradlew.bat bootRun
```

#### macOS

From the repository root:

```bash
chmod +x gradlew  # only needed if the executable bit was lost, e.g. after extracting a ZIP
./gradlew bootRun
```

#### Linux

Use the same commands as macOS:

```bash
chmod +x gradlew  # only if needed
./gradlew bootRun
```

The server starts at [http://localhost:8081](http://localhost:8081). The Gradle wrapper is included, so a local Gradle installation is not required. The first run downloads its pinned Gradle version and dependencies.

In Eclipse or Spring Tool Suite on any supported operating system, import the folder as an existing Gradle project and run `EcoembesApplication`.

### Tests and continuous integration

```bash
./gradlew test
```

The suite includes unit tests for garbage management and authentication, plus `MockMvc` tests for REST routes, parameters, JSON credentials and response codes. Integration tests also start the complete application on a random local port and verify OpenAPI and Swagger UI.

The [CI workflow](.github/workflows/ci.yml) runs the test suite for pushes to `master` and pull requests. The `master` branch requires the `test` check to pass before changes are integrated.

### Classroom notes and dependency review

Stable versions checked on **8 October 2026**:

| Component | Version | Official reference |
|:--|:--|:--|
| Spring Boot | 4.1.1 | [Releases](https://github.com/spring-projects/spring-boot/releases) |
| springdoc OpenAPI | 3.1.1 (Spring Boot 4 compatible branch) | [Documentation](https://springdoc.org/) |
| Spring dependency management plugin | 1.1.7 | [Releases](https://github.com/spring-gradle-plugins/dependency-management-plugin/releases) |
| Apache Commons Codec | 1.22.1 (explicit override) | [Release history](https://commons.apache.org/proper/commons-codec/changes.html) |
| Gradle wrapper | 9.8.0 | [Releases](https://gradle.org/releases/) |
| CI checkout / Java setup | 7.0.1 / 6.0.1 | [Checkout](https://github.com/actions/checkout/releases), [Java setup](https://github.com/actions/setup-java/releases) |

Java 21 remains the classroom requirement. Spring Boot manages the remaining library versions as a compatible set; they are not individually pinned to their newest upstream release. See [Spring Boot dependency management](https://docs.spring.io/spring-boot/gradle-plugin/managing-dependencies.html).

Pass the returned token as a **plain text body** when managing garbage or logging out.

V2 stores all data and sessions in memory: restarting resets them. The clients default to V2 at port 8082; configure their server URL to `http://localhost:8081` when using V1.

### License and authorship

This project is licensed under the [MIT License](LICENSE).

Faculty of Engineering, University of Deusto — Academic year 2026–27.

### AI assistance and review disclosure

The application code was initially generated with ChatGPT 4o (OpenAI) and adapted using GitHub Copilot. The codebase and documentation were reviewed and updated with assistance from Claude Opus 4.8 (Anthropic) in July 2026 and ChatGPT-6.1 Sol (OpenAI) in October 2026.

All automated tests and the continuous integration workflow were entirely generated with ChatGPT-6.1 (OpenAI) in September and October 2026.

---

## Español

### Descripción general

Ecoembes Server V1 es el backend inicial del caso docente *Ecoembes Service*, un sistema de gestión de residuos distribuido y simplificado para el proyecto de diseño del software de tercero de Ingeniería Informática. Gestiona basura, plantas, usuarios y categorías mediante una API REST de Spring Boot.

La versión es para la entrega de clase. Ilustra los patrones Façade, Application Service, Data Transfer Object y State Management.

### API REST

| Método | Endpoint | Descripción |
|:--|:--|:--|
| POST | `/auth/login` | Inicia sesión y obten un token |
| POST | `/auth/logout` | Cierra sesión con el token |
| POST | `/contenedores` | Crea un contenedor de basura (requiere sesión) |
| PUT | `/contenedores/{id}/estado` | Actualiza automáticamente el estado de llenado del contenedor |
| GET | `/contenedores/{id}/uso` | Recibe el nivel de uso del contenedor (requiere sesión) |
| GET | `/contenedores/{id}/estado` | Recibe el estado de llenado del contenedor (requiere sesión) |
| GET | `/contenedores/zona` | Recibe una lista de contenedores de una zona (requiere sesión) |
| GET | `/planta/capacidades` | Recibe la capacidad de todas las plantas (requiere sesión) |
| POST | `/asignaciones` | Crea una asignación entre contenedores y una planta de reciclaje (requiere sesión) |
| GET | `/asignaciones/{id}` | Recibe una asignación |
| GET | `/asignaciones` | Recibe todas las asignaciones |
| GET | `/alertas/saturacion` | Recibe la saturación de las plantas (requiere sesión) |
| POST | `/alertas/saturacion/evaluar` | Escanea el nivel de llenado de los contenedores y crea una alerta en base a eso (requiere sesión) |

- Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- Documento OpenAPI: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

### Requisitos y ejecución en Windows, macOS y Linux

Requiere JDK 21.

#### Windows

Desde PowerShell:

```powershell
.\gradlew.bat bootRun
```

#### macOS

Desde la raíz del repositorio:

```bash
chmod +x gradlew  # solo si se ha perdido el permiso, por ejemplo tras extraer un ZIP
./gradlew bootRun
```

#### Linux

Utiliza los mismos comandos que en macOS:

```bash
chmod +x gradlew  # solo si fuera necesario
./gradlew bootRun
```

El servidor queda disponible en [http://localhost:8081](http://localhost:8081). Se incluye el wrapper de Gradle, por lo que no es necesario instalar Gradle localmente. La primera ejecución descarga la versión fijada de Gradle y las dependencias.

En Eclipse o Spring Tool Suite, en cualquiera de los sistemas operativos admitidos, importa la carpeta como proyecto Gradle existente y ejecuta `AuctionsApplication`.

### Pruebas e integración continua

```bash
./gradlew test
```

La batería incluye pruebas gestión de basura y autenticación, además de pruebas `MockMvc` de rutas REST, parámetros, credenciales JSON y códigos de respuesta. Las pruebas de integración arrancan también la aplicación completa en un puerto local aleatorio y verifican OpenAPIy Swagger UI.

El [flujo de CI](.github/workflows/ci.yml) ejecuta las pruebas en cada cambio a `master` y en cada pull request. La rama `master` requiere que la comprobación `test` sea correcta antes de integrar cambios.

### Notas para clase y revisión de dependencias

Versiones estables comprobadas el **8 de octubre de 2026**:

| Componente | Versión | Referencia oficial |
|:--|:--|:--|
| Spring Boot | 4.1.1 | [Versiones](https://github.com/spring-projects/spring-boot/releases) |
| springdoc OpenAPI | 3.1.1 (rama compatible con Spring Boot 4) | [Documentación](https://springdoc.org/) |
| Plugin de gestión de dependencias Spring | 1.1.7 | [Versiones](https://github.com/spring-gradle-plugins/dependency-management-plugin/releases) |
| Apache Commons Codec | 1.22.1 (versión explícita) | [Historial](https://commons.apache.org/proper/commons-codec/changes.html) |
| Wrapper de Gradle | 9.8.0 | [Versiones](https://gradle.org/releases/) |
| CI checkout / configuración Java | 7.0.1 / 6.0.1 | [Checkout](https://github.com/actions/checkout/releases), [Java setup](https://github.com/actions/setup-java/releases) |

Se mantiene Java 21 como requisito docente. Spring Boot gestiona las versiones del resto de librerías como un conjunto compatible; no se fija individualmente cada una a su última versión publicada. Véase la [gestión de dependencias de Spring Boot](https://docs.spring.io/spring-boot/gradle-plugin/managing-dependencies.html).

Envía el token recibido como **cuerpo de texto plano** para gestionar la basura o cerrar sesión.

V2 conserva los datos y las sesiones en memoria: se reinician al arrancar de nuevo. Los clientes apuntan por defecto a V2 en el puerto 8082; configura su URL como `http://localhost:8081` para utilizarlos con V1.

### Licencia y autoría

Este proyecto se distribuye bajo la [licencia MIT](LICENSE).

Facultad de Ingeniería, Universidad de Deusto — Curso académico 2026–27.

### Declaración sobre asistencia de IA y revisión

El código de la aplicación se generó inicialmente con ChatGPT 4o (OpenAI) y se adaptó utilizando GitHub Copilot. El código y la documentación se revisaron y actualizaron con asistencia de Claude Opus 4.8 (Anthropic) en julio de 2026 y de ChatGPT-6.1 Sol (OpenAI) en octubre de 2026.

Todas las pruebas automáticas y el flujo de integración continua se generaron íntegramente con ChatGPT-6.1 (OpenAI) en septiembre y octubre de 2026.
