# API REST Documentada

## URL Base

Todas las pruebas oficiales deben realizarse mediante el API Gateway:

```txt
http://localhost:8030/api
```

## Seguridad

Todas las rutas protegidas usan:

```http
Authorization: Bearer TOKEN
```

Rutas publicas:

- `POST /api/auth/login`
- `POST /api/auth/register-cliente`

## Auth Service

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response 200:

```json
{
  "token": "...",
  "rol": "ROLE_ADMIN",
  "idcliente": 1
}
```

### Registro de cliente

```http
POST /api/auth/register-cliente
```

Request:

```json
{
  "username": "cliente1",
  "password": "cliente123",
  "nombres": "Cliente",
  "apellidos": "Prueba",
  "correo": "cliente1@saga.local",
  "telefono": "900000001"
}
```

Response 200:

```json
{
  "mensaje": "Cliente registrado correctamente",
  "idusuario": 2,
  "idcliente": 2,
  "username": "cliente1",
  "rol": "ROLE_CLIENTE"
}
```

## Clientes

### Listar clientes

```http
GET /api/clientes
```

Rol recomendado: `ROLE_ADMIN`.

### Obtener cliente

```http
GET /api/clientes/{id}
```

### Buscar cliente por correo

```http
GET /api/clientes/correo/{correo}
```

### Validar cliente activo

```http
GET /api/clientes/{id}/validar
```

Uso interno de Saga.

### Crear cliente

```http
POST /api/clientes
```

Request:

```json
{
  "nombres": "Sergio",
  "apellidos": "Gabriel",
  "correo": "sergio@test.com",
  "telefono": "999999999",
  "estado": "A"
}
```

### Actualizar cliente

```http
PUT /api/clientes/{id}
```

### Desactivar cliente

```http
PUT /api/clientes/{id}/desactivar
```

Response 200:

```json
{
  "idcliente": 2,
  "estado": "I"
}
```

Si se intenta desactivar el cliente administrador:

```json
{
  "message": "El cliente administrador no se puede desactivar",
  "status": 400
}
```

### Activar cliente

```http
PUT /api/clientes/{id}/activar
```

### Eliminar cliente

```http
DELETE /api/clientes/{id}
```

Nota: no elimina fisicamente; realiza baja logica llamando a desactivar.

## Productos

### Listar productos

```http
GET /api/productos
```

### Obtener producto

```http
GET /api/productos/{id}
```

### Validar stock

```http
GET /api/productos/{id}/validar-stock/{cantidad}
```

Uso interno de Saga.

### Crear producto

```http
POST /api/productos
```

Request:

```json
{
  "nombre": "Papa",
  "precio": 3.5,
  "stock": 100,
  "estado": "A"
}
```

### Actualizar producto

```http
PUT /api/productos/{id}
```

### Desactivar producto

```http
PUT /api/productos/{id}/desactivar
```

### Activar producto

```http
PUT /api/productos/{id}/activar
```

### Aumentar stock

```http
PUT /api/productos/{id}/aumentar/{cantidad}
```

### Descontar stock

```http
PUT /api/productos/{id}/descontar/{cantidad}
```

### Descontar stock para Saga

```http
PUT /api/productos/{id}/descontar-stock/{cantidad}
```

### Restaurar stock para compensacion

```http
PUT /api/productos/{id}/restaurar-stock/{cantidad}
```

### Eliminar producto

```http
DELETE /api/productos/{id}
```

## Ventas

### Listar ventas

```http
GET /api/ventas
```

Rol recomendado: `ROLE_ADMIN`.

### Mis ventas

```http
GET /api/ventas/mis-ventas
```

Usa `idcliente` del token.

### Obtener venta

```http
GET /api/ventas/{id}
```

### Crear venta normal

```http
POST /api/ventas
```

Internamente usa Saga simple.

### Venta Saga simple

```http
POST /api/ventas/saga
```

Request:

```json
{
  "idcliente": 1,
  "idproducto": 1,
  "cantidad": 2
}
```

### Venta Saga del cliente autenticado

```http
POST /api/ventas/saga/cliente
```

No se envia `idcliente`; se toma del token.

Request:

```json
{
  "idproducto": 1,
  "cantidad": 2
}
```

### Venta Saga con carrito

```http
POST /api/ventas/saga/carrito/cliente
```

Request:

```json
{
  "items": [
    {
      "idproducto": 1,
      "cantidad": 2
    },
    {
      "idproducto": 2,
      "cantidad": 1
    }
  ]
}
```

Response 200:

```json
{
  "sagaId": "uuid",
  "estado": "COMPLETADA",
  "mensaje": "Compra realizada correctamente.",
  "ventas": [],
  "total": 11.2
}
```

Error por stock:

```json
{
  "message": "No hay stock suficiente para completar tu compra.",
  "status": 409
}
```

Error por cliente inactivo:

```json
{
  "message": "Tu cuenta de cliente esta inactiva. No puedes realizar compras.",
  "status": 409
}
```

### Logs de Saga

```http
GET /api/ventas/saga-logs
```

Rol recomendado: `ROLE_ADMIN`.

Response:

```json
[
  {
    "id": 1,
    "sagaId": "uuid",
    "idcliente": 2,
    "tipo": "CARRITO",
    "estado": "FALLIDA",
    "pasoFallido": "VALIDAR_CLIENTE",
    "mensajeCliente": "Tu cuenta de cliente esta inactiva. No puedes realizar compras.",
    "detalleTecnico": "Received: Bad Request...",
    "stockCompensado": false,
    "fecha": "2026-06-27T22:40:12"
  }
]
```

## Codigos HTTP

- `200`: operacion correcta.
- `400`: solicitud incorrecta o regla de negocio invalida.
- `401`: token ausente o invalido.
- `403`: rol sin permiso.
- `404`: recurso no encontrado.
- `409`: conflicto funcional, por ejemplo compra no completada.
- `500`: error interno no controlado.

## Evidencias de Prueba de API

- Captura de Postman importando coleccion.
- Captura de login exitoso.
- Captura de listar productos por gateway.
- Captura de Saga carrito exitosa.
- Captura de Saga fallida por stock.
- Captura de Saga fallida por cliente inactivo.
- Captura de `GET /api/ventas/saga-logs`.


