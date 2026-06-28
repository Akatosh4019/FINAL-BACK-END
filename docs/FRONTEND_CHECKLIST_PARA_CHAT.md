# Texto para Enviar al Chat del Frontend

```txt
Necesito que revises si mi frontend Angular cumple los entregables del proyecto final.

Backend principal:
http://localhost:8030/api

El backend ya tiene:
- Login JWT.
- ROLE_ADMIN y ROLE_CLIENTE.
- idcliente dentro del login.
- CRUD de clientes.
- CRUD de productos.
- Ventas.
- Saga con carrito.
- Saga logs para admin.
- Cliente admin protegido contra desactivacion.
- Cliente normal puede ser desactivado.
- Cliente inactivo no puede comprar.

Rutas importantes:

Login:
POST /api/auth/login
Body admin:
{
  "username": "admin",
  "password": "admin123"
}

Registro cliente:
POST /api/auth/register-cliente
Body:
{
  "username": "cliente1",
  "password": "cliente123",
  "nombres": "Cliente",
  "apellidos": "Prueba",
  "correo": "cliente1@saga.local",
  "telefono": "900000001"
}

Productos:
GET /api/productos
POST /api/productos
PUT /api/productos/{id}
PUT /api/productos/{id}/activar
PUT /api/productos/{id}/desactivar
PUT /api/productos/{id}/aumentar/{cantidad}
DELETE /api/productos/{id}  -> desactiva producto, no borra fisicamente

Clientes:
GET /api/clientes
GET /api/clientes/{id}
PUT /api/clientes/{id}
PUT /api/clientes/{id}/activar
PUT /api/clientes/{id}/desactivar
DELETE /api/clientes/{id}

Regla importante:
- No mostrar boton desactivar/eliminar para el cliente admin.
- Si backend responde "El cliente administrador no se puede desactivar", mostrar mensaje claro.

Ventas:
GET /api/ventas
GET /api/ventas/mis-ventas
POST /api/ventas/saga/carrito/cliente
GET /api/ventas/saga-logs

Body de carrito:
{
  "items": [
    { "idproducto": 1, "cantidad": 2 },
    { "idproducto": 2, "cantidad": 1 }
  ]
}

Respuesta compra exitosa:
{
  "sagaId": "...",
  "estado": "COMPLETADA",
  "mensaje": "Compra realizada correctamente.",
  "ventas": [],
  "total": 11.2
}

Errores importantes:

Stock insuficiente:
HTTP 409
{
  "message": "No hay stock suficiente para completar tu compra.",
  "status": 409
}

Cliente inactivo:
HTTP 409
{
  "message": "Tu cuenta de cliente esta inactiva. No puedes realizar compras.",
  "status": 409
}

Necesito que revises si el frontend cumple:

1. Login admin y cliente.
2. Separacion de vistas por rol.
3. Admin puede ver dashboard.
4. Admin puede gestionar productos.
5. Admin puede listar clientes.
6. Admin puede activar/desactivar clientes normales.
7. Admin no puede desactivar cliente administrador o no se muestra la opcion.
8. Admin puede ver ventas.
9. Admin puede ver saga logs.
10. Cliente puede ver productos.
11. Cliente puede usar carrito.
12. Cliente puede comprar por /api/ventas/saga/carrito/cliente.
13. Cliente ve mensaje bonito si no hay stock.
14. Cliente ve mensaje bonito si esta inactivo.
15. El token JWT se manda en Authorization Bearer.
16. Todas las llamadas van por gateway http://localhost:8030/api.
17. Existe README con instrucciones para correr.
18. El build funciona con npm run build.

Tambien dime que capturas debo sacar para evidencias del entregable Frontend e Integracion Frontend-Backend.
```


