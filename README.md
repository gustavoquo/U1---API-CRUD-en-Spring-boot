# Gestión de Usuarios y Autenticación Básica con Spring Boot

API REST desarrollada con Spring Boot para registrar usuarios, iniciar sesión y administrar usuarios mediante operaciones CRUD. El proyecto aplica arquitectura en capas, inyección de dependencias por constructor, validaciones, respuestas HTTP adecuadas y hashing de contraseñas con BCrypt.

## Parte 1. Investigación teórica

### 1. Arquitectura en capas

En una aplicación Spring Boot orientada a usuarios, cada capa tiene una responsabilidad específica:

- **Controller:** recibe las peticiones HTTP, obtiene parámetros o cuerpos JSON, ejecuta validaciones de entrada y devuelve respuestas HTTP mediante `ResponseEntity`. No debe contener la lógica principal del negocio.
- **Service:** contiene la lógica de negocio. En este proyecto se encarga, por ejemplo, de registrar usuarios, comprobar si un email ya existe, validar credenciales, actualizar usuarios y desactivar cuentas.
- **Repository:** se comunica con la base de datos. Spring Data JPA permite realizar operaciones como guardar, buscar, actualizar y consultar usuarios sin escribir SQL manual para los casos básicos.
- **Entity:** representa el modelo persistente de la aplicación. La entidad `Usuario` se relaciona con la tabla `usuarios` y contiene los campos `id`, `nombre`, `email`, `password` y `estado`.

El flujo general es:

`Petición HTTP -> Controller -> Service -> Repository -> Base de datos`

La respuesta recorre el camino inverso hasta llegar al cliente.

### 2. Manejo de contraseñas

Las contraseñas nunca deben almacenarse en texto plano porque, si la base de datos fuera expuesta, todas las credenciales quedarían disponibles directamente para un atacante.

Un algoritmo de hashing como **BCrypt** transforma la contraseña en un valor hash que no está pensado para revertirse. Además, BCrypt utiliza salt y un costo computacional configurable, lo que dificulta ataques de fuerza bruta y el uso de tablas precalculadas.

Al registrar un usuario se almacena el hash generado por BCrypt. Durante el login no se desencripta la contraseña: se utiliza `PasswordEncoder.matches()` para comprobar si la contraseña recibida corresponde al hash almacenado.

### 3. Diferencia entre Registro y Login

**Registro:** crea un usuario nuevo. El sistema valida los datos, comprueba que el email no esté registrado, aplica hashing a la contraseña y guarda el usuario en la base de datos.

**Login:** no crea ningún registro. Busca al usuario por email y verifica que la contraseña proporcionada coincida con el hash almacenado. Si coincide y la cuenta está activa, el acceso se considera válido; en caso contrario se responde con `401 Unauthorized`.

### 4. Buenas prácticas REST

| Operación | Método y endpoint | Código exitoso | Errores principales |
|---|---|---:|---|
| Registrar usuario | `POST /api/v1/auth/register` | `201 Created` | `400 Bad Request` si faltan datos o el email ya existe |
| Iniciar sesión | `POST /api/v1/auth/login` | `200 OK` | `400 Bad Request` por datos inválidos / `401 Unauthorized` por credenciales incorrectas |
| Listar usuarios | `GET /api/v1/users` | `200 OK` | — |
| Obtener usuario | `GET /api/v1/users/{id}` | `200 OK` | `404 Not Found` si el usuario no existe |
| Actualizar usuario | `PUT /api/v1/users/{id}` | `200 OK` | `400 Bad Request` por datos inválidos o email duplicado / `404 Not Found` |
| Desactivar usuario | `DELETE /api/v1/users/{id}` | `200 OK` | `404 Not Found` si el usuario no existe |

`POST` se utiliza para crear recursos o ejecutar operaciones como el login, `GET` para consultar información, `PUT` para actualizar un recurso identificado y `DELETE` para eliminarlo o desactivarlo.

## Parte 2. Implementación

### Tecnologías utilizadas

- Java 17
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security Crypto / BCrypt
- H2 Database
- Maven

> Se utiliza únicamente el módulo criptográfico de Spring Security para BCrypt. No se configuró autenticación automática de Spring Security porque el objetivo de la práctica es implementar manualmente la lógica básica de registro y login.

## Estructura de paquetes

```text
src/main/java/com/ejemplo/usuarios
├── UsuariosApplication.java
├── config
│   └── PasswordConfig.java
├── controller
│   ├── AuthController.java
│   └── UsuarioController.java
├── dto
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   ├── RegisterRequest.java
│   ├── UpdateUserRequest.java
│   └── UserResponse.java
├── entity
│   ├── EstadoUsuario.java
│   └── Usuario.java
├── exception
│   └── GlobalExceptionHandler.java
├── repository
│   └── UsuarioRepository.java
└── service
    ├── AuthService.java
    └── UsuarioService.java
```

## Modelo de datos

La entidad `Usuario` contiene:

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador autogenerado |
| `nombre` | `String` | Nombre del usuario |
| `email` | `String` | Email único |
| `password` | `String` | Contraseña almacenada como hash BCrypt |
| `estado` | `EstadoUsuario` | `ACTIVO` o `INACTIVO` |

La contraseña existe en la entidad porque debe almacenarse en la base de datos, pero **nunca se devuelve en las respuestas de la API**. Para las respuestas se utiliza `UserResponse`.

## Ejecución local

### Requisitos

- JDK 17 o superior compatible
- Maven 3.9 o superior
- Git, opcional para clonar el repositorio

### Pasos

1. Clonar el repositorio:

```bash
git clone URL_DE_TU_REPOSITORIO
cd usuarios
```

2. Ejecutar la aplicación:

```bash
mvn spring-boot:run
```

También puede compilarse primero:

```bash
mvn clean package
java -jar target/usuarios-0.0.1-SNAPSHOT.jar
```

3. La aplicación estará disponible en:

```text
http://localhost:8080
```

La base de datos H2 funciona en memoria, por lo que los datos se reinician al detener la aplicación.

### Consola H2

URL:

```text
http://localhost:8080/h2-console
```

Configuración:

```text
JDBC URL: jdbc:h2:mem:usuariosdb
User Name: sa
Password: [vacío]
```

## Ejemplos de peticiones HTTP

### 1. Registrar usuario

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "password": "secreto123"
  }'
```

Respuesta esperada: `201 Created`

```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "estado": "ACTIVO"
}
```

Si el email ya existe, la API responde con `400 Bad Request`.

### 2. Iniciar sesión

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "secreto123"
  }'
```

Respuesta esperada: `200 OK`

```json
{
  "mensaje": "Login exitoso",
  "usuario": {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "estado": "ACTIVO"
  }
}
```

Con contraseña incorrecta:

```text
401 Unauthorized
```

```json
{
  "mensaje": "Credenciales inválidas"
}
```

### 3. Obtener todos los usuarios

```bash
curl -i http://localhost:8080/api/v1/users
```

Respuesta esperada: `200 OK`.

### 4. Obtener usuario por ID

```bash
curl -i http://localhost:8080/api/v1/users/1
```

Si el ID no existe, la API responde con `404 Not Found`.

### 5. Actualizar usuario

```bash
curl -i -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez Actualizado",
    "email": "juan.actualizado@example.com"
  }'
```

Respuesta esperada: `200 OK`.

La contraseña no se modifica desde este endpoint porque el requerimiento especifica actualizar únicamente nombre y email.

### 6. Desactivar usuario

```bash
curl -i -X DELETE http://localhost:8080/api/v1/users/1
```

Respuesta esperada: `200 OK`

```json
{
  "mensaje": "Usuario desactivado correctamente"
}
```

Después de desactivarlo, el registro continúa en la base de datos con estado `INACTIVO`, pero ya no puede iniciar sesión.

## Validaciones implementadas

- Nombre obligatorio.
- Email obligatorio y con formato válido.
- Email único al registrar.
- Email único al actualizar.
- Contraseña obligatoria y mínimo de 6 caracteres durante el registro.
- `400 Bad Request` ante datos de entrada inválidos.
- `401 Unauthorized` ante credenciales incorrectas o usuario inactivo.
- `404 Not Found` cuando el ID solicitado no existe.

## Inyección de dependencias

Todas las dependencias se reciben mediante constructor. Por ejemplo:

```java
public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
}
```

No se utiliza `@Autowired` directamente sobre atributos.

## Seguridad de contraseñas

El encoder se configura como un bean:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Al registrar:

```java
passwordEncoder.encode(request.getPassword())
```

Al iniciar sesión:

```java
passwordEncoder.matches(request.getPassword(), usuario.getPassword())
```

## Observación sobre autenticación

Este proyecto implementa una **autenticación básica de práctica**: el endpoint de login verifica las credenciales y devuelve un mensaje de éxito, tal como solicita el ejercicio. No mantiene una sesión ni genera tokens.

En un sistema real normalmente se agregaría un mecanismo posterior de autenticación y autorización, por ejemplo sesiones seguras u OAuth2/JWT, dependiendo de los requisitos del sistema.

## Referencias

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Spring Security - Password Storage: https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
- BCryptPasswordEncoder: https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html
- HTTP Status Codes - MDN: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
