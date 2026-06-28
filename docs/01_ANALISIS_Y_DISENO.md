# Documento de Analisis y Diseno

## Proyecto

Sistema distribuido de ventas con microservicios, API Gateway, seguridad JWT, descubrimiento con Consul y consistencia distribuida mediante Saga Pattern.

## Contexto

El proyecto resuelve el problema de gestionar clientes, productos y ventas en una arquitectura de microservicios. Cada servicio mantiene su propia responsabilidad y su propia base de datos, evitando acoplamiento directo entre dominios. El flujo critico del sistema es la venta, porque necesita validar cliente, validar stock, descontar producto y registrar la venta sin dejar datos inconsistentes.

Aunque la guia original menciona tecnologias como Spring Cloud Config y Eureka, el docente permitio trabajar con Quarkus y Consul. Por eso la solucion mantiene Quarkus en los microservicios y usa Consul para registro/descubrimiento.

## Problema

En sistemas distribuidos no existe una unica transaccion local que cubra todas las bases de datos. Si una venta descuenta stock y luego falla al registrar la venta, el sistema puede quedar inconsistente. Ademas, se necesita controlar acceso por roles para que un administrador gestione informacion y un cliente solo pueda comprar.

## Justificacion

La arquitectura de microservicios permite separar responsabilidades, escalar servicios individualmente y mantener bases de datos independientes. Para resolver la consistencia se usa Saga Orchestration en `ms.ventas`, que coordina llamadas REST hacia `ms.cliente` y `ms.producto`. Si ocurre un error despues de descontar stock, la Saga ejecuta compensacion restaurando el stock.

## Objetivo General

Implementar un sistema distribuido funcional para ventas, aplicando microservicios con Quarkus, API Gateway, JWT, Consul, Docker y Saga Pattern.

## Objetivos Especificos

- Implementar microservicios independientes para autenticacion, clientes, productos y ventas.
- Proteger rutas mediante JWT, roles y permisos.
- Exponer una entrada principal mediante API Gateway.
- Registrar servicios en Consul para descubrimiento.
- Mantener base de datos independiente por microservicio.
- Implementar Saga Orchestration para el proceso de venta.
- Agregar compensacion de stock cuando una venta falla luego del descuento.
- Registrar logs de Saga para auditoria administrativa.
- Dockerizar todo el entorno para ejecucion reproducible.

## Alcance

Incluye:

- Login de administrador y cliente.
- Registro publico de cliente.
- CRUD de clientes, productos y ventas.
- Activacion/desactivacion logica de clientes y productos.
- Cliente administrador protegido contra desactivacion.
- Compra simple y compra con carrito mediante Saga.
- Validacion de cliente activo.
- Validacion de producto y stock.
- Descuento y restauracion de stock.
- Logs administrativos de Saga.
- Docker Compose con bases y servicios.

No incluye:

- Pasarela de pago real.
- Kafka o RabbitMQ.
- JWT externo con proveedor OAuth.
- Kubernetes.
- Frontend dentro del mismo repositorio backend.

## Arquitectura

Servicios:

- `api.gateway`: entrada principal del sistema, rutas, filtro JWT y permisos.
- `ms.auth`: login, registro de cliente, roles y permisos.
- `ms.cliente`: gestion de clientes.
- `ms.producto`: gestion de productos y stock.
- `ms.ventas`: gestion de ventas y orquestacion Saga.
- `central-config`: archivos de configuracion montados en contenedores.
- `consul`: registro y descubrimiento.

Bases de datos:

- MySQL: `ms.auth` y `ms.cliente`.
- Oracle: `ms.producto`.
- PostgreSQL: `ms.ventas`.

## Requerimientos Funcionales

- RF01: El usuario administrador puede iniciar sesion.
- RF02: Un cliente puede registrarse y obtener usuario asociado.
- RF03: El gateway valida token JWT antes de permitir acceso.
- RF04: El administrador puede listar, crear, actualizar y desactivar clientes.
- RF05: El administrador no puede desactivar al cliente administrador.
- RF06: El administrador puede gestionar productos.
- RF07: El cliente puede listar productos.
- RF08: El cliente puede comprar mediante Saga.
- RF09: El sistema valida que el cliente exista y este activo antes de comprar.
- RF10: El sistema valida que el producto exista y tenga stock suficiente.
- RF11: El sistema descuenta stock durante una venta exitosa.
- RF12: El sistema registra ventas exitosas.
- RF13: Si ocurre un error despues de descontar stock, se restaura el stock.
- RF14: El administrador puede consultar historial de Saga.
- RF15: El cliente inactivo no puede realizar compras.

## Requerimientos No Funcionales

- RNF01: Los servicios deben ejecutarse en contenedores Docker.
- RNF02: Cada microservicio debe mantener su propia base de datos.
- RNF03: La comunicacion entre servicios se realiza mediante REST.
- RNF04: La entrada principal debe ser el API Gateway.
- RNF05: Deben existir logs tecnicos para seguimiento de Saga.
- RNF06: El sistema debe manejar errores con codigos HTTP claros.
- RNF07: El sistema debe usar Consul para registro de servicios.
- RNF08: El backend debe mantenerse en Quarkus para microservicios.

## Casos de Uso

### CU01 - Iniciar sesion

Actor: Administrador o cliente.

Flujo:

1. El usuario envia credenciales.
2. `ms.auth` valida usuario, password y estado.
3. Se genera token JWT con rol e `idcliente`.
4. El frontend guarda el token.

### CU02 - Registrar cliente

Actor: Cliente.

Flujo:

1. El cliente envia datos personales y credenciales.
2. `ms.auth` crea el cliente en `ms.cliente`.
3. `ms.auth` crea el usuario con rol `ROLE_CLIENTE`.
4. El sistema responde con id de usuario e id de cliente.

### CU03 - Comprar con carrito

Actor: Cliente.

Flujo:

1. El cliente agrega productos al carrito.
2. El frontend envia el carrito a `POST /api/ventas/saga/carrito/cliente`.
3. `ms.ventas` inicia la Saga.
4. `ms.ventas` valida cliente activo en `ms.cliente`.
5. `ms.ventas` valida stock en `ms.producto`.
6. `ms.ventas` descuenta stock.
7. `ms.ventas` registra ventas.
8. `ms.ventas` registra Saga completada.

Flujo alterno:

- Si el cliente esta inactivo, se cancela la compra.
- Si no hay stock, se cancela la compra.
- Si falla luego del descuento, se restaura stock.

### CU04 - Consultar logs de Saga

Actor: Administrador.

Flujo:

1. El administrador ingresa al panel.
2. Solicita `GET /api/ventas/saga-logs`.
3. Visualiza Saga completadas y fallidas.

## Historias de Usuario

- HU01: Como administrador quiero iniciar sesion para gestionar el sistema.
- HU02: Como cliente quiero registrarme para poder comprar productos.
- HU03: Como cliente quiero ver productos disponibles para elegir que comprar.
- HU04: Como cliente quiero comprar uno o varios productos para completar una venta.
- HU05: Como cliente quiero recibir un mensaje claro si mi compra falla.
- HU06: Como administrador quiero ver clientes para gestionarlos.
- HU07: Como administrador quiero desactivar clientes sin eliminarlos fisicamente.
- HU08: Como administrador quiero evitar que el cliente administrador sea desactivado.
- HU09: Como administrador quiero consultar logs de Saga para auditar fallos.
- HU10: Como administrador quiero ver ventas realizadas.

## Reglas de Negocio

- El administrador puede entrar al panel administrador y tambien comprar como cliente.
- Un cliente normal no puede acceder al panel administrador.
- Un cliente inactivo no puede comprar.
- El cliente administrador no puede desactivarse.
- `DELETE /clientes/{id}` funciona como baja logica, no como eliminacion fisica.
- Una venta solo se registra si cliente y stock son validos.
- La compensacion de stock se ejecuta si el error ocurre despues de descontar stock.

## Evidencias Sugeridas

- Captura de login.
- Captura de servicios en Docker.
- Captura de Consul con servicios registrados.
- Captura de Postman con Saga exitosa.
- Captura de Postman con error por stock.
- Captura de Postman con cliente inactivo.
- Captura de `saga-logs`.
- Captura de gateway usando rutas `/api`.

