# Diagramas C4

## Nota Tecnica

El proyecto usa microservicios Quarkus, Spring Cloud Gateway como API Gateway y Consul como registro/descubrimiento. Esta decision fue permitida por el docente, aunque la guia base mencione otras tecnologias.

## C1 - Context Diagram

```mermaid
flowchart LR
    Cliente["Cliente / Usuario Final"]
    Admin["Administrador"]
    Sistema["Sistema de Ventas Distribuido\nQuarkus + Gateway + Consul"]
    DBs["Bases de datos por microservicio\nMySQL / Oracle / PostgreSQL"]

    Cliente -->|"Login, consulta productos, compra"| Sistema
    Admin -->|"Gestion clientes, productos, ventas, logs"| Sistema
    Sistema -->|"Persistencia independiente"| DBs
```

Descripcion:

El sistema permite a clientes comprar productos y al administrador gestionar el negocio. La comunicacion externa entra por el API Gateway. Los microservicios mantienen bases separadas para cumplir independencia de datos.

## C2 - Container Diagram

```mermaid
flowchart TB
    U["Cliente / Admin"]
    GW["api.gateway\nSpring Cloud Gateway\nPuerto 8030"]
    AUTH["ms.auth\nQuarkus\nPuerto 8084"]
    CLI["ms.cliente\nQuarkus\nPuerto 8082"]
    PROD1["ms.producto\nQuarkus\nPuerto 8080"]
    PROD2["ms.producto-2\nQuarkus\nPuerto 8085"]
    VEN["ms.ventas\nQuarkus\nPuerto 8083"]
    CONSUL["Consul\nPuerto 8500"]
    MYSQL["MySQL\nauth_db / cliente"]
    ORACLE["Oracle\nproducto"]
    PG["PostgreSQL\nventas"]
    CONFIG["central-config\narchivos properties"]

    U --> GW
    GW --> AUTH
    GW --> CLI
    GW --> PROD1
    GW --> PROD2
    GW --> VEN

    AUTH --> MYSQL
    CLI --> MYSQL
    PROD1 --> ORACLE
    PROD2 --> ORACLE
    VEN --> PG

    VEN -->|"REST Client"| CLI
    VEN -->|"REST Client"| PROD1
    VEN -->|"REST Client / LB via Gateway-Consul"| PROD2

    GW --> CONSUL
    AUTH --> CONSUL
    CLI --> CONSUL
    PROD1 --> CONSUL
    PROD2 --> CONSUL
    VEN --> CONSUL

    AUTH --- CONFIG
    CLI --- CONFIG
    PROD1 --- CONFIG
    VEN --- CONFIG
```

Descripcion:

El gateway enruta todas las solicitudes oficiales bajo `/api`. Consul registra los servicios y permite balanceo hacia instancias como `ms-producto` y `ms-producto-2`. Cada microservicio tiene su base correspondiente.

## C3 - Component Diagram: ms.ventas

```mermaid
flowchart TB
    VC["VentaController"]
    VS["VentaServiceImpl\nOrquestador Saga"]
    VR["VentaRepository"]
    SLS["SagaLogServiceImpl"]
    SLR["SagaLogRepository"]
    PC["ProductoClient\nREST Client"]
    CC["ClienteClient\nREST Client"]
    PG["PostgreSQL\nventa / saga_log"]
    CLI["ms.cliente"]
    PROD["ms.producto"]

    VC --> VS
    VS --> VR
    VS --> SLS
    SLS --> SLR
    VR --> PG
    SLR --> PG
    VS --> CC
    VS --> PC
    CC --> CLI
    PC --> PROD
```

Descripcion:

`ms.ventas` contiene el orquestador de la Saga. Valida cliente, valida stock, descuenta stock, registra ventas y ejecuta compensacion si corresponde. Tambien guarda logs de Saga para auditoria.

## C3 - Component Diagram: ms.auth

```mermaid
flowchart TB
    AC["AuthController"]
    SC["SeguridadController"]
    AS["AuthServiceImpl"]
    SS["SeguridadServiceImpl"]
    JWT["JwtService"]
    UR["UsuarioRepository"]
    RR["RolRepository / PermisoRepository"]
    CC["ClienteClient"]
    MYSQL["MySQL auth_db"]
    CLI["ms.cliente"]

    AC --> AS
    SC --> SS
    AS --> JWT
    AS --> UR
    AS --> CC
    SS --> RR
    UR --> MYSQL
    RR --> MYSQL
    CC --> CLI
```

Descripcion:

`ms.auth` gestiona login, registro de clientes, roles y permisos. Al registrar un cliente, crea primero el cliente en `ms.cliente` y luego el usuario con `ROLE_CLIENTE`.

## C3 - Component Diagram: api.gateway

```mermaid
flowchart TB
    REQ["Request /api/**"]
    SEC["SecurityConfig"]
    JWT["JwtAuthenticationFilter"]
    UTIL["JwtUtil"]
    PERM["PermisoClient"]
    ROUTES["Gateway Routes"]
    AUTH["ms.auth"]
    CLI["ms.cliente"]
    PROD["ms.producto"]
    VEN["ms.ventas"]

    REQ --> SEC
    SEC --> JWT
    JWT --> UTIL
    JWT --> PERM
    JWT --> ROUTES
    PERM --> AUTH
    ROUTES --> AUTH
    ROUTES --> CLI
    ROUTES --> PROD
    ROUTES --> VEN
```

Descripcion:

El gateway valida JWT, obtiene permisos del rol y enruta a los microservicios. Las rutas oficiales para pruebas y frontend son las del gateway.

## C4 - Code Diagram: Saga de Venta con Carrito

```mermaid
sequenceDiagram
    participant F as Frontend/Postman
    participant G as API Gateway
    participant V as ms.ventas
    participant C as ms.cliente
    participant P as ms.producto
    participant DB as PostgreSQL

    F->>G: POST /api/ventas/saga/carrito/cliente
    G->>G: Validar JWT y permisos
    G->>V: Enviar request con X-Cliente-Id
    V->>V: Crear sagaId
    V->>C: GET /clientes/{id}/validar
    C-->>V: Cliente activo
    V->>P: GET /productos/{id}/validar-stock/{cantidad}
    P-->>V: Producto con stock
    V->>P: PUT /productos/{id}/descontar-stock/{cantidad}
    P-->>V: Stock descontado
    V->>DB: Persistir venta(s)
    V->>DB: Persistir saga_log COMPLETADA
    V-->>G: Compra realizada correctamente
    G-->>F: 200 OK
```

## C4 - Code Diagram: Compensacion

```mermaid
sequenceDiagram
    participant V as ms.ventas
    participant P as ms.producto
    participant DB as PostgreSQL

    V->>P: Descontar stock
    P-->>V: OK
    V->>DB: Registrar venta
    DB--xV: Error
    V->>P: Restaurar stock
    P-->>V: Stock restaurado
    V->>DB: Registrar saga_log FALLIDA
```

## Evidencias del Modelado

- Captura del PDF C4 generado.
- Captura de Consul con servicios registrados.
- Captura del flujo Saga exitosa en Postman.
- Captura de Saga fallida y `saga-logs`.


