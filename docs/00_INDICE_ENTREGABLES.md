# Indice de entregables del proyecto

Este documento sirve como mapa rapido para ubicar los entregables principales del proyecto final de microservicios.

## Documentos generados

| Entregable | Archivo |
| --- | --- |
| Analisis y diseno | `docs/01_ANALISIS_Y_DISENO.md` |
| Diagramas C4 | `docs/02_DIAGRAMAS_C4.md` |
| API REST documentada | `docs/05_API_REST_DOCUMENTADA.md` |
| Manual tecnico | `docs/17_MANUAL_TECNICO.md` |
| Manual de usuario | `docs/18_MANUAL_USUARIO.md` |
| Guia para video de sustentacion | `docs/19_GUIA_VIDEO_SUSTENTACION.md` |
| Evidencias de pruebas y ejecucion | `docs/20_EVIDENCIAS_PRUEBAS.md` |
| Contexto para el chat de frontend | `docs/FRONTEND_CHECKLIST_PARA_CHAT.md` |

## Nota tecnica importante

El proyecto usa Quarkus en los microservicios y Consul para registro/descubrimiento. Esto es valido porque el docente permitio mantener Quarkus y aceptar Consul como alternativa a Eureka/Spring Cloud del enunciado base.

## Capturas para la sustentacion

1. Docker Desktop o `docker compose ps` con todos los contenedores levantados.
2. Consul UI mostrando servicios registrados: gateway, auth, cliente, producto, ventas.
3. API Gateway respondiendo rutas `/api/auth`, `/api/clientes`, `/api/productos` y `/api/ventas`.
4. Login con JWT y uso del token en Postman.
5. CRUD de clientes, productos y ventas desde el gateway.
6. Saga exitosa: venta registrada y stock descontado.
7. Saga fallida por stock insuficiente.
8. Saga fallida despues del descuento, mostrando compensacion y restauracion de stock.
9. Logs de Saga desde endpoint de administracion.
10. Circuit breaker, fallback y timeout en ventas.
11. Balanceo de carga con dos instancias de producto.
12. Frontend: login admin, login cliente, carrito, compra exitosa, error de compra y panel admin.

## Orden de exposicion

1. Problema de consistencia distribuida.
2. Arquitectura general con microservicios.
3. API Gateway, JWT, Consul y central-config.
4. Persistencia independiente por microservicio.
5. Saga orchestration en `ms.ventas`.
6. Compensacion de stock en `ms.producto`.
7. Resiliencia: timeout, fallback y circuit breaker.
8. Balanceo de carga en producto.
9. Frontend consumiendo el backend.
10. Pruebas y cierre.



