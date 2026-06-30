# Informe Final del Proyecto

## Sistema de ventas con microservicios, Quarkus, API Gateway, Consul y Saga Pattern

**Autor:** Samuel Valencia  
**Repositorio Backend:** FINAL-BACK-END  
**Repositorio Frontend:** FINAL-FRONT-END  
**Tecnologias principales:** Quarkus, Docker, Consul, REST Client, JWT, MySQL/Oracle/PostgreSQL, Angular  

---

## 1. Resumen Ejecutivo

El proyecto implementa un sistema distribuido de ventas basado en microservicios. La arquitectura mantiene servicios independientes para autenticacion, clientes, productos y ventas, todos desplegados en contenedores Docker y registrados en Consul para descubrimiento de servicios.

La operacion principal del sistema es la realizacion de una venta mediante el patron **Saga Orchestration**, donde `ms-ventas` coordina el flujo completo: validacion del cliente, validacion del producto, descuento de stock y registro de venta. Si ocurre un error despues del descuento de stock, se ejecuta una compensacion para restaurar el stock.

El sistema tambien incluye seguridad con JWT, API Gateway centralizado, auditoria de Saga logs, manejo de errores funcionales, balanceo mediante dos instancias de `ms-producto`, y un frontend Angular para cliente y administrador.

---

## 2. Descripcion del Problema

En una arquitectura de microservicios, cada servicio posee su propia responsabilidad y su propia fuente de datos. Esto evita acoplamiento fuerte, pero genera un reto importante: mantener consistencia entre operaciones distribuidas.

En el caso del proyecto, una venta depende de varios servicios:

| Paso | Servicio | Responsabilidad |
|---|---|---|
| 1 | `ms-ventas` | Iniciar y coordinar la venta |
| 2 | `ms-cliente` | Validar que el cliente exista y este activo |
| 3 | `ms-producto` | Validar producto y stock disponible |
| 4 | `ms-producto` | Descontar stock |
| 5 | `ms-ventas` | Registrar la venta |

Si el stock se descuenta pero luego falla el registro de la venta, el sistema quedaria inconsistente. Por ello se implementa una Saga con compensacion.

---

## 3. Justificacion Tecnica

Se eligio mantener la arquitectura existente en Quarkus porque el proyecto ya estaba dividido en microservicios funcionales. En lugar de reemplazar servicios, se agrego la coordinacion Saga en `ms-ventas`, respetando la estructura original.

Decisiones principales:

| Decision | Justificacion |
|---|---|
| Mantener Quarkus | El proyecto ya estaba implementado con Quarkus y el requisito indicaba no cambiarlo |
| Usar REST Client | Permite comunicacion sincrona entre microservicios sin usar Kafka o RabbitMQ |
| Usar Saga Orchestration | `ms-ventas` coordina el flujo y centraliza la compensacion |
| Usar Consul | Permite descubrimiento de servicios y evidencia de instancias activas |
| Usar API Gateway | Centraliza el acceso externo a los microservicios |
| Mantener JWT | Protege endpoints administrativos y operaciones del sistema |
| No eliminar clientes fisicamente | Los clientes estan asociados a usuarios y ventas |
| Eliminar productos solo sin ventas | Conserva trazabilidad historica de boletas y ventas |

---

## 4. Objetivos

### Objetivo General

Implementar un sistema de ventas distribuido con microservicios en Quarkus, incorporando Saga Pattern para mantener consistencia de stock y ventas ante fallos.

### Objetivos Especificos

- Implementar autenticacion y autorizacion con JWT.
- Centralizar acceso mediante API Gateway.
- Registrar servicios en Consul.
- Implementar CRUD de clientes y productos.
- Implementar venta coordinada con Saga Orchestration.
- Implementar compensacion de stock ante errores.
- Registrar logs de Saga para auditoria.
- Probar errores funcionales como cliente inactivo y stock insuficiente.
- Implementar frontend Angular para administrador y cliente.
- Documentar evidencias, endpoints, arquitectura y pruebas.

---

## 5. Alcance

El sistema cubre:

- Gestion de usuarios y autenticacion.
- Gestion de clientes.
- Gestion de productos.
- Gestion de ventas.
- Compra mediante Saga.
- Compra por carrito desde frontend.
- Panel administrativo.
- Auditoria de operaciones Saga.
- Despliegue dockerizado.
- Descubrimiento de servicios con Consul.

No incluye pagos reales externos, porque el alcance academico del proyecto no requiere integracion con pasarelas de pago.

---

## 6. Requerimientos Funcionales

| Codigo | Requerimiento | Estado |
|---|---|---|
| RF-01 | Login con JWT | Implementado |
| RF-02 | Registrar cliente junto a usuario | Implementado |
| RF-03 | Listar clientes | Implementado |
| RF-04 | Actualizar cliente | Implementado |
| RF-05 | Activar/desactivar cliente | Implementado |
| RF-06 | Crear producto | Implementado |
| RF-07 | Listar productos | Implementado |
| RF-08 | Actualizar producto | Implementado |
| RF-09 | Activar/desactivar producto | Implementado |
| RF-10 | Eliminar producto sin ventas | Implementado |
| RF-11 | Bloquear eliminacion de producto con ventas | Implementado |
| RF-12 | Realizar venta con Saga | Implementado |
| RF-13 | Validar cliente activo | Implementado |
| RF-14 | Validar stock disponible | Implementado |
| RF-15 | Descontar stock | Implementado |
| RF-16 | Restaurar stock ante fallo posterior al descuento | Implementado |
| RF-17 | Registrar Saga logs | Implementado |
| RF-18 | Mostrar historial de compras | Implementado |

---

## 7. Requerimientos No Funcionales

| Codigo | Requerimiento | Implementacion |
|---|---|---|
| RNF-01 | Seguridad | JWT con roles |
| RNF-02 | Disponibilidad | Servicios dockerizados |
| RNF-03 | Descubrimiento | Consul |
| RNF-04 | Resiliencia | Circuit Breaker, Fallback y Timeout |
| RNF-05 | Trazabilidad | Saga logs |
| RNF-06 | Escalabilidad | Dos instancias de `ms-producto` |
| RNF-07 | Mantenibilidad | Separacion por microservicios |
| RNF-08 | Consistencia eventual | Saga con compensacion |

---

## 8. Casos de Uso

| Caso de Uso | Actor | Descripcion |
|---|---|---|
| CU-01 Login | Administrador / Cliente | Iniciar sesion y recibir token JWT |
| CU-02 Gestionar clientes | Administrador | Listar, actualizar, activar y desactivar clientes |
| CU-03 Gestionar productos | Administrador | Crear, listar, actualizar, activar, desactivar y eliminar productos permitidos |
| CU-04 Comprar producto | Cliente | Realizar compra usando Saga |
| CU-05 Comprar desde admin | Administrador | Realizar compra como cliente/admin |
| CU-06 Ver ventas | Administrador | Consultar ventas globales |
| CU-07 Ver Saga logs | Administrador | Auditar ventas exitosas y fallidas |
| CU-08 Ver mis compras | Cliente | Consultar historial personal |

---

## 9. Historias de Usuario

| Historia | Descripcion | Criterio de Aceptacion |
|---|---|---|
| HU-01 | Como administrador quiero iniciar sesion para gestionar el sistema | El sistema retorna JWT con rol `ROLE_ADMIN` |
| HU-02 | Como cliente quiero crear cuenta para comprar productos | Se crea usuario y cliente asociado |
| HU-03 | Como cliente quiero comprar productos disponibles | La venta se registra y se descuenta stock |
| HU-04 | Como cliente quiero recibir mensaje si no hay stock | Se muestra error controlado |
| HU-05 | Como administrador quiero ver logs de Saga | Se listan errores, pasos fallidos y compensaciones |
| HU-06 | Como administrador quiero evitar borrar productos vendidos | El sistema bloquea eliminacion con HTTP 409 |

---

## 10. Arquitectura C4

### 10.1 Nivel 1: Contexto

```mermaid
C4Context
title Sistema de Ventas con Saga Pattern - Contexto
Person(cliente, "Cliente", "Compra productos desde la tienda web")
Person(admin, "Administrador", "Gestiona clientes, productos, ventas y auditoria")
System(sistema, "Sistema Saga Store", "Sistema distribuido de ventas con microservicios")
System_Ext(github, "GitHub", "Repositorio del proyecto")
Rel(cliente, sistema, "Compra productos y consulta compras")
Rel(admin, sistema, "Administra catalogo, ventas y Saga logs")
Rel(admin, github, "Publica y versiona el codigo")
```

### 10.2 Nivel 2: Contenedores

```mermaid
C4Container
title Sistema de Ventas - Contenedores
Person(cliente, "Cliente", "Usuario comprador")
Person(admin, "Administrador", "Usuario administrador")
Container(frontend, "Frontend Angular", "Angular", "Interfaz web para cliente y administrador")
Container(gateway, "api-gateway", "Quarkus", "Entrada centralizada a APIs")
Container(auth, "ms-auth", "Quarkus", "Login, usuarios, roles y JWT")
Container(clienteMs, "ms-cliente", "Quarkus", "Gestion y validacion de clientes")
Container(productoMs, "ms-producto", "Quarkus", "Gestion de productos y stock")
Container(productoMs2, "ms-producto-2", "Quarkus", "Segunda instancia para balanceo")
Container(ventasMs, "ms-ventas", "Quarkus", "Orquestador Saga y ventas")
Container(consul, "Consul", "HashiCorp Consul", "Service discovery")
ContainerDb(dbAuth, "BD Auth", "MySQL/Oracle", "Usuarios y roles")
ContainerDb(dbCliente, "BD Cliente", "MySQL/Oracle", "Clientes")
ContainerDb(dbProducto, "BD Producto", "MySQL/Oracle", "Productos y stock")
ContainerDb(dbVentas, "BD Ventas", "PostgreSQL", "Ventas y Saga logs")
Rel(cliente, frontend, "Usa")
Rel(admin, frontend, "Usa")
Rel(frontend, gateway, "Consume REST con JWT")
Rel(gateway, auth, "Rutea login")
Rel(gateway, clienteMs, "Rutea clientes")
Rel(gateway, productoMs, "Rutea productos")
Rel(gateway, ventasMs, "Rutea ventas y Saga")
Rel(ventasMs, clienteMs, "Valida cliente con REST Client")
Rel(ventasMs, productoMs, "Valida/descuenta/restaura stock con REST Client")
Rel(auth, dbAuth, "Lee/escribe")
Rel(clienteMs, dbCliente, "Lee/escribe")
Rel(productoMs, dbProducto, "Lee/escribe")
Rel(ventasMs, dbVentas, "Lee/escribe")
Rel(gateway, consul, "Descubre servicios")
Rel(productoMs, consul, "Registra instancia")
Rel(productoMs2, consul, "Registra instancia")
```

### 10.3 Nivel 3: Componentes de `ms-ventas`

```mermaid
C4Component
title ms-ventas - Componentes
Container_Boundary(ventas, "ms-ventas") {
  Component(ventaController, "VentaController", "REST Controller", "Expone endpoints de ventas y Saga")
  Component(ventaService, "VentaServiceImpl", "Service", "Coordina Saga Orchestration")
  Component(sagaLogService, "SagaLogServiceImpl", "Service", "Registra auditoria Saga")
  Component(ventaRepo, "VentaRepository", "Repository", "Persistencia de ventas")
  Component(sagaRepo, "SagaLogRepository", "Repository", "Persistencia de logs")
  Component(clienteClient, "ClienteClient", "REST Client", "Comunica con ms-cliente")
  Component(productoClient, "ProductoClient", "REST Client", "Comunica con ms-producto")
}
Rel(ventaController, ventaService, "Invoca")
Rel(ventaService, clienteClient, "Valida cliente")
Rel(ventaService, productoClient, "Valida/descuenta/restaura stock")
Rel(ventaService, ventaRepo, "Registra venta")
Rel(ventaService, sagaLogService, "Registra eventos")
Rel(sagaLogService, sagaRepo, "Persiste logs")
```

### 10.4 Nivel 4: Codigo Relevante

Fragmento de resiliencia en `VentaServiceImpl`:

```java
@CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 5000
)
@Fallback(fallbackMethod = "fallbackProducto")
@Timeout(3000)
public ProductoDTO obtenerProducto(Long id) {
    return productoClient.buscarProductoPorId(id);
}
```

Fragmento de compensacion:

```java
try {
    productoClient.descontarStock(request.idproducto, request.cantidad);
    if (simularFalloDespuesDescuento) {
        throw new RuntimeException("Error simulado despues de descontar stock");
    }
    return registrarVenta(request, sagaId);
} catch (Exception ex) {
    productoClient.restaurarStock(request.idproducto, request.cantidad);
    registrarSagaFallida(sagaId, "ERROR_SIMULADO_POST_DESCUENTO", true);
    throw new ConflictException("No se pudo completar tu compra. Intenta nuevamente.");
}
```

---

## 11. Microservicios Implementados

| Microservicio | Responsabilidad | Puerto |
|---|---|---|
| `api-gateway` | Entrada centralizada | 8030 |
| `ms-auth` | Login, JWT, usuarios y roles | 8084 |
| `ms-cliente` | Gestion y validacion de clientes | 8082 |
| `ms-producto` | Productos y stock | 8080 |
| `ms-producto-2` | Segunda instancia de productos | 8085 |
| `ms-ventas` | Ventas, Saga y auditoria | 8083 |
| `central-config` | Configuracion centralizada | N/A |
| `consul` | Registro y descubrimiento | 8500 |

---

## 12. API REST Documentada

### Autenticacion

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/api/auth/login` | Login y generacion de JWT |

### Clientes

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/clientes` | Listar clientes |
| GET | `/api/clientes/{id}` | Buscar cliente |
| PUT | `/api/clientes/{id}` | Actualizar cliente |
| PUT | `/api/clientes/{id}/activar` | Activar cliente |
| PUT | `/api/clientes/{id}/desactivar` | Desactivar cliente |

### Productos

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/productos` | Listar productos |
| GET | `/api/productos/{id}` | Buscar producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| PUT | `/api/productos/{id}/activar` | Activar producto |
| PUT | `/api/productos/{id}/desactivar` | Desactivar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto solo si no tiene ventas |

### Ventas y Saga

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/ventas` | Listar ventas |
| GET | `/api/ventas/{id}` | Buscar venta |
| POST | `/api/ventas/saga` | Ejecutar venta Saga admin |
| POST | `/api/ventas/saga/carrito/cliente` | Ejecutar compra cliente por carrito |
| GET | `/api/ventas/saga-logs` | Listar auditoria Saga |
| GET | `/api/ventas/producto/{idproducto}/conteo` | Contar ventas asociadas a producto |

---

## 13. Evidencias de Implementacion

### 13.1 Docker

La siguiente evidencia muestra los contenedores del proyecto ejecutandose en Docker Desktop:

![Docker completo](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-d549c25f-e989-4c96-a9bc-4601bc3d6fa1.png)

### 13.2 Consul y Service Discovery

Consul muestra todos los servicios registrados y saludables:

![Consul servicios](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-3c55917f-27b9-42f9-b334-c4af2c0063f5.png)

El servicio `ms-producto` registra dos instancias:

![Consul producto dos instancias](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-4cfe9b34-768f-4d21-b95b-2748b0423753.png)

### 13.3 JWT

Login del administrador por API Gateway:

![Login JWT Postman](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-a9d2ee66-45eb-4c9b-9ea5-38c46bafe482.png)

### 13.4 CRUD Clientes

Listar clientes:

![Listar clientes](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-3ba089c2-47e6-4b1b-b3d2-b2ba4f2d3db4.png)

Actualizar cliente:

![Actualizar cliente](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-a4bf8f0c-d52e-4d95-9f02-ba45b8efbbba.png)

Desactivar cliente:

![Desactivar cliente](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-bce01cf9-8758-47fe-9119-607848ebfe32.png)

Activar cliente:

![Activar cliente](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-a66140bc-2e78-4a0e-b0ba-de567eb56641.png)

### 13.5 CRUD Productos

Crear producto:

![Crear producto](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-74ac4de1-5867-4603-91f8-3be273168d0f.png)

Listar productos:

![Listar productos](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-5e2cc350-5980-4022-83e8-0423b4adf19b.png)

Actualizar producto:

![Actualizar producto](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-1df58e7b-970b-4263-9d6e-2e105c075720.png)

Eliminar producto con ventas bloqueado:

![Eliminar producto con ventas](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-cee15dd6-e585-43a2-bf3d-35f4a8cda0ed.png)

### 13.6 Ventas y Saga

Listado de ventas:

![Listar ventas](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-072c9aa2-3c78-4ad6-a985-e1352a5fc52e.png)

Venta Saga exitosa:

![Venta Saga exitosa](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-5b0e078c-04c1-4f86-99d9-77aad4abb599.png)

Error por stock insuficiente:

![Error stock insuficiente](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-005e609e-9217-46bd-b873-347d1f54ef05.png)

Error por cliente inactivo:

![Error cliente inactivo](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-df247cbc-fa45-4922-a5df-872ccc513b36.png)

### 13.7 Compensacion Saga

Stock antes:

![Stock antes compensacion](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-4c9b47ca-7a37-4fb0-b89c-fd956b4225af.png)

Error simulado despues del descuento:

![Error simulado compensacion](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-3bcc9ec4-d7be-4b8a-a3a5-4c6f326d70d1.png)

Stock restaurado:

![Stock restaurado](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-c596542c-22d0-497e-b61d-7c2792331c85.png)

### 13.8 Saga Logs

Backend:

![Saga logs Postman](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-261a8b3c-153d-43e2-9c43-b3b99a6f3fb4.png)

Frontend:

![Saga logs frontend](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-9ad65a07-77af-4f42-b08f-114289e01a3d.png)

### 13.9 Resiliencia

Codigo con Circuit Breaker, Fallback y Timeout:

![Circuit breaker codigo](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-1a383005-090c-4da5-9c19-ef7ef1d9c0d3.png)

---

## 14. Frontend Angular

El frontend permite dos flujos principales:

- Cliente: login, registro, tienda, carrito y mis compras.
- Administrador: panel general, clientes, productos, ventas, Saga logs y prueba de Saga.

Pantalla de login cliente:

![Login cliente](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-cba9341f-20ed-4e03-8e37-e95102397f4a.png)

Pantalla de login administrador:

![Login admin](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-e477f53b-ff9c-4f8f-9230-bb34f47d3367.png)

Panel administrativo:

![Panel administrativo](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-efb205e2-e76d-4bc7-8162-bf0a00e021f6.png)

Gestion de clientes:

![Frontend clientes](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-91b3a7e3-fd71-4afd-9722-8e42eff17bb6.png)

Gestion de productos:

![Frontend productos](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-e4075fac-1a8e-4496-9871-fe55a6ac85af.png)

Ventas globales:

![Frontend ventas](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-014148e6-b21d-4e37-a764-b97e51d8be78.png)

Saga admin:

![Frontend saga admin](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-09bfcc2f-78c3-44f5-89e1-33fe65874557.png)

Tienda y carrito:

![Tienda carrito](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-eea299df-1ce3-4fcc-b359-9e80c40d7e59.png)

Mis compras:

![Mis compras](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-a1455cd4-6339-4ea5-91cd-156e1ffb7ca9.png)

---

## 15. Repositorios Git

El proyecto fue dividido en repositorio backend y frontend.

Vista general de repositorios:

![Repositorios GitHub](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-c1678d88-d6ab-4300-ad1b-502308b90d50.png)

Repositorio frontend:

![Repo frontend](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-b639cf3b-410d-414a-b9f5-92cb86047f89.png)

Repositorio backend:

![Repo backend](C:/Users/LENOVO/AppData/Local/Temp/codex-clipboard-7f1f9b1f-04c7-4b4c-8a85-bd1bc3f32acd.png)

---

## 16. Manual Tecnico

### 16.1 Requisitos

- Docker Desktop.
- Java 17 o superior.
- Maven.
- Node.js 18 o superior para frontend.
- Postman.

### 16.2 Ejecucion Backend

Desde la raiz del proyecto backend:

```bash
docker compose up -d --build
```

Servicios principales:

| Servicio | URL |
|---|---|
| API Gateway | `http://localhost:8030` |
| Consul | `http://localhost:8500` |
| ms-auth | `http://localhost:8084` |
| ms-cliente | `http://localhost:8082` |
| ms-producto | `http://localhost:8080` |
| ms-producto-2 | `http://localhost:8085` |
| ms-ventas | `http://localhost:8083` |

### 16.3 Ejecucion Frontend

Desde el proyecto frontend:

```bash
npm install
npm start
```

URL:

```text
http://localhost:4200
```

### 16.4 Login de Prueba

| Usuario | Clave | Rol |
|---|---|---|
| admin | admin123 | ROLE_ADMIN |

---

## 17. Manual de Usuario

### Administrador

1. Ingresar por el boton Admin.
2. Iniciar sesion con usuario administrador.
3. Revisar el resumen del panel.
4. Gestionar clientes.
5. Gestionar productos.
6. Consultar ventas.
7. Revisar Saga logs.
8. Probar ventas Saga y compensacion desde Saga admin.

### Cliente

1. Crear cuenta o iniciar sesion.
2. Ver catalogo de productos.
3. Agregar productos al carrito.
4. Confirmar compra.
5. Revisar historial en Mis compras.

---

## 18. Guion Sugerido Para Sustentacion

1. Presentar el problema de consistencia en microservicios.
2. Mostrar arquitectura general y microservicios.
3. Mostrar Docker con contenedores activos.
4. Mostrar Consul con servicios registrados.
5. Explicar el API Gateway.
6. Mostrar login JWT.
7. Mostrar CRUD clientes y productos.
8. Explicar decision de no eliminar clientes y proteger productos vendidos.
9. Mostrar venta Saga exitosa.
10. Mostrar error por stock insuficiente.
11. Mostrar error por cliente inactivo.
12. Mostrar compensacion de stock.
13. Mostrar Saga logs.
14. Mostrar Circuit Breaker/Fallback/Timeout.
15. Mostrar frontend cliente y administrador.
16. Mostrar GitHub y README.
17. Cerrar explicando beneficios: consistencia, trazabilidad, resiliencia y despliegue dockerizado.

---

## 19. Conclusiones

El proyecto cumple con la implementacion de una arquitectura de microservicios en Quarkus, manteniendo servicios separados y comunicacion mediante REST Client. La incorporacion de Saga Orchestration permite manejar operaciones distribuidas de venta con compensacion de stock ante fallos.

La solucion conserva la trazabilidad mediante Saga logs, protege operaciones con JWT, usa Consul para discovery, Docker para despliegue y Angular como interfaz final. Ademas, se aplicaron decisiones de integridad como no eliminar clientes y bloquear la eliminacion de productos que ya tienen ventas registradas.

---

## 20. Anexos

- Coleccion Postman del proyecto.
- Repositorio backend.
- Repositorio frontend.
- Capturas de Docker, Consul, Postman, codigo y frontend.
- Diagramas C4.
