# Guia para Video de Sustentacion

Duracion recomendada: 8 a 12 minutos.

## Orden del Video

### 1. Presentacion Inicial

Decir:

```txt
Presentamos un sistema distribuido de ventas desarrollado con microservicios Quarkus, API Gateway, Consul, bases de datos por servicio, seguridad JWT y Saga Pattern para consistencia distribuida.
```

Mostrar:

- Nombre del proyecto.
- Integrantes.
- Repositorio GitHub.

### 2. Problema y Objetivo

Explicar:

- El problema de consistencia en microservicios.
- Por que una venta puede fallar entre validar cliente, descontar stock y registrar venta.
- Objetivo: mantener datos consistentes usando Saga Orchestration.

Mostrar:

- Documento de analisis y diseno.

### 3. Arquitectura General

Mostrar:

- Diagrama C4 C1.
- Diagrama C4 C2.

Explicar:

- Gateway como entrada principal.
- Microservicios Quarkus.
- Consul como registro/descubrimiento.
- Base de datos por microservicio.

Frase clave:

```txt
Aunque la guia base menciona Eureka y Spring Cloud Config, el docente permitio usar Quarkus y Consul. Por eso se mantiene la arquitectura con Consul como descubrimiento y archivos central-config montados en Docker.
```

### 4. Docker y Servicios

Mostrar:

```bash
docker compose ps
```

Mostrar:

- `api-gateway`
- `ms-auth`
- `ms-cliente`
- `ms-producto`
- `ms-producto-2`
- `ms-ventas`
- `consul`
- bases de datos

### 5. Consul

Abrir:

```txt
http://localhost:8500
```

Mostrar:

- Servicios registrados.
- Especialmente `ms-producto` con dos instancias.

Nota para sustentar:

- Esta captura sirve para registro/descubrimiento y balanceo de carga.

### 6. API Gateway y Seguridad

Mostrar:

- `application.yml` del gateway.
- Rutas `/api/auth`, `/api/clientes`, `/api/productos`, `/api/ventas`.
- Filtro JWT.

Luego en Postman:

1. Login admin.
2. Copiar/mostrar token guardado.
3. Listar productos por gateway.

### 7. Roles y Permisos

Explicar:

- `ROLE_ADMIN`: gestion completa.
- `ROLE_CLIENTE`: productos, compras y mis ventas.
- El gateway valida token y permisos.

Mostrar:

- Login admin.
- Login cliente si ya existe uno.

### 8. CRUD Principal

Mostrar rapidamente:

- Listar productos.
- Crear o editar producto.
- Listar clientes.
- Desactivar cliente normal.

Importante:

- Mostrar que el cliente admin no se puede desactivar.

### 9. Saga Exitosa

En Postman:

```http
POST /api/ventas/saga/carrito/cliente
```

Body:

```json
{
  "items": [
    { "idproducto": 1, "cantidad": 2 },
    { "idproducto": 2, "cantidad": 1 }
  ]
}
```

Mostrar:

- Respuesta `COMPLETADA`.
- `sagaId`.
- Total.
- Ventas creadas.
- Stock descontado.

### 10. Saga Fallida por Stock

Enviar cantidad enorme:

```json
{
  "items": [
    { "idproducto": 1, "cantidad": 999999 }
  ]
}
```

Mostrar:

- Error 409.
- Mensaje: stock insuficiente.
- Stock no queda negativo.
- Saga log fallido.

### 11. Saga Fallida por Cliente Inactivo

Pasos:

1. Crear cliente normal.
2. Desactivarlo desde admin.
3. Iniciar sesion como cliente.
4. Intentar comprar.

Mostrar:

- Error: `Tu cuenta de cliente esta inactiva...`
- Saga log con `VALIDAR_CLIENTE`.

### 12. Compensacion

Explicar:

```txt
Si el error ocurre despues de descontar stock, ms.ventas llama a ms.producto para restaurar el stock descontado.
```

Mostrar si se tiene evidencia:

- Logs de `ms.ventas`.
- `saga_log` con `stockCompensado`.

### 13. Resiliencia

Mostrar codigo en:

```txt
ms.ventas/src/main/java/pe/edu/upeu/serviceImpl/VentaServiceImpl.java
```

Capturar:

- `@CircuitBreaker`
- `@Fallback`
- `@Timeout`

Nota para sustentar:

- Esta evidencia corresponde al entregable 10 de resiliencia.

### 14. Balanceo de Carga

Mostrar:

- `ms-producto`
- `ms-producto-2`
- Consul con dos instancias.
- Gateway usando `lb://ms-producto`.

Nota para sustentar:

- Esta evidencia corresponde al entregable 11 de balanceo de carga.

### 15. Frontend

Mostrar:

- Login.
- Dashboard admin.
- Vista cliente.
- Productos.
- Carrito.
- Compra.
- Saga logs en admin.

### 16. Cierre

Decir:

```txt
El sistema demuestra microservicios independientes, seguridad JWT, API Gateway, descubrimiento con Consul, Dockerizacion, balanceo de carga, resiliencia y consistencia distribuida con Saga Pattern.
```

## Checklist de Capturas para el Video

- Docker Compose con contenedores arriba.
- Consul con servicios registrados.
- Gateway routes.
- Login admin.
- Login cliente.
- Listar productos por gateway.
- Saga exitosa.
- Saga fallida por stock.
- Saga fallida por cliente inactivo.
- Saga logs.
- Codigo de CircuitBreaker/Fallback/Timeout.
- Dos instancias de producto para balanceo.
- Frontend login/dashboard/carrito.


