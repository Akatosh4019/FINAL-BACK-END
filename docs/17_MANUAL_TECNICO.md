# Manual Tecnico

## Tecnologias

- Java 21.
- Quarkus para microservicios.
- Spring Cloud Gateway para API Gateway.
- Consul para registro y descubrimiento.
- MySQL para `ms.auth` y `ms.cliente`.
- Oracle para `ms.producto`.
- PostgreSQL para `ms.ventas`.
- Docker y Docker Compose.
- REST Client de Quarkus.
- JWT para autenticacion y autorizacion.

## Estructura del Proyecto

```txt
api.gateway/
ms.auth/
ms.cliente/
ms.producto/
ms.ventas/
central-config/
docker/
postman/
tools/
docs/
docker-compose.yml
```

## Puertos

- API Gateway: `8030`.
- Consul: `8500`.
- ms.producto: `8080`.
- ms.producto-2: `8085`.
- ms.cliente: `8082`.
- ms.ventas: `8083`.
- ms.auth: `8084`.
- MySQL host: `3307`.
- PostgreSQL host: `5433`.
- Oracle host: `1522`.

## Ejecucion con Docker

Desde la raiz:

```bash
docker compose up -d
```

Si algunos servicios Quarkus inician antes que las bases y se detienen, esperar 30 a 60 segundos y ejecutar:

```bash
docker compose up -d ms-cliente ms-auth ms-producto ms-producto-2 consul-registrator api-gateway
```

Ver estado:

```bash
docker compose ps
```

Reiniciar datos desde cero:

```bash
docker compose down
docker compose up -d
```

## Datos Iniciales

Al iniciar desde cero:

- Usuario administrador:
  - username: `admin`
  - password: `admin123`
  - rol: `ROLE_ADMIN`
  - idcliente: `1`
- Productos iniciales:
  - Papa
  - Arroz
  - Aceite
- Ventas iniciales: `0`.
- Saga logs iniciales: `0`.

## Configuracion Centralizada

La carpeta `central-config` contiene archivos `.properties` usados por los microservicios. En Docker se monta como volumen:

```txt
./central-config:/app/central-config
```

El proyecto no usa Spring Cloud Config porque el stack permitido para los microservicios es Quarkus. La configuracion se mantiene centralizada a nivel de archivos compartidos y montados en contenedores.

## Registro y Descubrimiento

Consul corre en:

```txt
http://localhost:8500
```

Los servicios se registran usando `consul-registrator` en Docker Compose. Servicios registrados:

- `ms-producto`
- `ms-producto` segunda instancia con ID `ms-producto-2`
- `ms-cliente`
- `ms-auth`
- `ms-ventas`

## API Gateway

Entrada principal:

```txt
http://localhost:8030/api
```

Rutas:

- `/api/auth/**` hacia `ms-auth`.
- `/api/clientes/**` hacia `ms-cliente`.
- `/api/productos/**` hacia `ms-producto`.
- `/api/ventas/**` hacia `ms-ventas`.

El gateway valida JWT y permisos antes de enrutar.

## Seguridad

Flujo:

1. Usuario envia credenciales a `/api/auth/login`.
2. `ms.auth` valida credenciales y rol.
3. `ms.auth` genera JWT.
4. El frontend envia `Authorization: Bearer TOKEN`.
5. `api.gateway` valida token.
6. `api.gateway` consulta permisos del rol.
7. Si tiene permiso, enruta al microservicio correspondiente.

Roles:

- `ROLE_ADMIN`: puede gestionar clientes, productos, ventas y seguridad.
- `ROLE_CLIENTE`: puede ver productos, comprar y consultar sus ventas.

## Saga Pattern

El orquestador esta en `ms.ventas`.

Flujo:

1. Inicia Saga.
2. Valida cliente activo en `ms.cliente`.
3. Valida producto y stock en `ms.producto`.
4. Descuenta stock.
5. Registra venta.
6. Registra log de Saga.

Compensacion:

- Si falla despues de descontar stock, `ms.ventas` llama a `ms.producto` para restaurar stock.

Endpoints principales:

- `POST /api/ventas/saga`
- `POST /api/ventas/saga/cliente`
- `POST /api/ventas/saga/carrito/cliente`
- `GET /api/ventas/saga-logs`

## Resiliencia

`ms.ventas` usa:

- `@CircuitBreaker`
- `@Fallback`
- `@Timeout`

Aplicado sobre consultas a cliente/producto. Sirve para tolerar fallos y responder de forma controlada si un servicio no esta disponible.

Evidencia generada en `docs/20_EVIDENCIAS_PRUEBAS.md`: codigo con `@CircuitBreaker`, `@Fallback`, `@Timeout`, prueba con `ms-cliente` detenido y respuesta controlada de la Saga.

## Balanceo de Carga

El balanceo se evidencia con dos instancias de producto:

- `ms-producto`
- `ms-producto-2`

Ambas se registran en Consul con el mismo nombre logico `ms-producto`.

Evidencia generada en `docs/20_EVIDENCIAS_PRUEBAS.md`: Docker y Consul muestran dos instancias saludables de `ms-producto`. En la sustentacion se explica el uso del nombre logico del servicio para enrutar peticiones.

## Postman

Archivos:

- `postman/saga-quarkus.postman_collection.json`
- `postman/saga-quarkus.postman_environment.json`
- `postman/README.md`

Uso:

1. Importar ambiente.
2. Importar coleccion.
3. Ejecutar login.
4. Usar rutas por gateway.
5. Probar Saga exitosa y errores.

## Comandos Utiles

Logs:

```bash
docker logs ms-ventas --tail 120
docker logs ms-producto --tail 120
docker logs api-gateway --tail 120
```

Estado:

```bash
docker compose ps
```

Compilar un microservicio:

```bash
mvn -DskipTests "-Dquarkus.config.locations=" package
```

## Problemas Comunes

### Un servicio queda en Exited

Causa probable: arranco antes que la base de datos.

Solucion:

```bash
docker compose up -d ms-cliente ms-auth ms-producto ms-producto-2 consul-registrator api-gateway
```

### Error 401

Token ausente, invalido o expirado.

### Error 403

El usuario tiene token valido, pero no tiene permiso para la ruta.

### Error 409 en compra

La Saga rechazo la compra por regla funcional: stock insuficiente, cliente inactivo u otro conflicto.



