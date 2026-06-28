# Manual de Usuario

## Acceso al Sistema

El sistema se consume mediante el frontend o Postman usando el API Gateway:

```txt
http://localhost:8030/api
```

## Usuario Administrador

Credenciales:

```txt
username: admin
password: admin123
```

El administrador puede:

- Ver productos.
- Crear productos.
- Editar productos.
- Activar o desactivar productos.
- Ver clientes.
- Crear o editar clientes.
- Desactivar clientes normales.
- Ver ventas.
- Ver logs de Saga.
- Comprar como cliente administrador.

Importante:

- El cliente administrador no puede ser desactivado.
- El administrador puede entrar al panel administrador y tambien al apartado cliente.

## Usuario Cliente

El cliente puede registrarse desde el formulario de registro.

El cliente puede:

- Iniciar sesion.
- Ver productos.
- Agregar productos al carrito.
- Comprar productos.
- Ver sus ventas.

El cliente no puede:

- Entrar al panel administrador.
- Crear productos.
- Editar productos.
- Ver todos los clientes.
- Ver logs tecnicos de Saga.

## Flujo de Compra

1. Iniciar sesion como cliente.
2. Ver productos disponibles.
3. Agregar productos al carrito.
4. Confirmar compra.
5. El sistema valida cliente y stock.
6. Si todo es correcto, se registra la compra.
7. Si hay un error, se muestra un mensaje claro.

## Mensajes Comunes

Compra exitosa:

```txt
Compra realizada correctamente.
```

Stock insuficiente:

```txt
No hay stock suficiente para completar tu compra.
```

Cliente inactivo:

```txt
Tu cuenta de cliente esta inactiva. No puedes realizar compras.
```

Sin permiso:

```txt
No tienes permisos para realizar esta accion.
```

## Panel Administrador

### Productos

Acciones:

- Listar productos.
- Crear producto.
- Editar producto.
- Activar producto.
- Desactivar producto.
- Aumentar stock.

### Clientes

Acciones:

- Listar clientes.
- Editar cliente.
- Activar cliente.
- Desactivar cliente.

Regla:

- No mostrar boton de desactivar para el cliente administrador.

### Ventas

Acciones:

- Ver historial de ventas.
- Revisar ventas por cliente.

### Saga Logs

Acciones:

- Ver Saga completadas.
- Ver Saga fallidas.
- Revisar paso fallido.
- Revisar mensaje de cliente.
- Revisar detalle tecnico.
- Revisar si hubo compensacion de stock.

## Recomendaciones de Uso

- Usar siempre las rutas por gateway.
- Iniciar sesion antes de probar rutas protegidas.
- Si el token expira, volver a iniciar sesion.
- Para simular error de stock, comprar una cantidad mayor al stock disponible.
- Para simular cliente inactivo, desactivar un cliente normal desde admin y luego intentar comprar con ese cliente.

