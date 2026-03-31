Sistema de Biblioteca Serverless

Descripción
Este proyecto implementa un sistema de gestión de biblioteca basado en arquitectura Serverless utilizando:
- BFF (Spring Boot)
- Azure Functions en Java
- Oracle Autonomous Database

Arquitectura
Cliente (Postman)
    ↓
BFF (Spring Boot)
    ↓
Azure Functions (Java)
    ↓
Oracle Database

Componentes
- BFF: Orquesta las llamadas a las funciones
- Azure Functions: Lógica de negocio
- Oracle DB: Almacenamiento de datos

Endpoints principales

Usuarios
POST /bff/usuarios
GET /bff/usuarios
PUT /bff/usuarios
DELETE /bff/usuarios?id=1

Préstamos
POST /bff/prestamos
GET /bff/prestamos
PUT /bff/prestamos
DELETE /bff/prestamos?id=1

Libros
GET /bff/libros

Ejemplos JSON

Crear Usuario
{
  "nombre": "Diego",
  "apellidos": "Morales",
  "correo": "diego@mail.com",
  "password": "123456"
}

Crear Préstamo
{
  "id_usuario": "1",
  "id_libro": "1"
}

Devolver Préstamo
{
  "id": "1"
}

Configuración
function.url=https://TU-FUNCTION.azurewebsites.net/api

Variables de entorno:
DB_USER
DB_PASSWORD
DB_URL

Docker
docker build -t bff .
docker run -p 8080:8080 bff

Tecnologías
Java, Spring Boot, Azure Functions, Oracle DB, Docker

Estado del proyecto
CRUD completo de usuarios y préstamos funcionando.

Autor
Diego Morales Alfaro
