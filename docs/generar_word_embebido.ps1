$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "docs"
$work = Join-Path $outDir "_word_embebido_tmp"
$docx = Join-Path $outDir "INFORME_FINAL_PROYECTO_MICROSERVICIOS_EMBEBIDO.docx"

if (Test-Path $work) { Remove-Item -LiteralPath $work -Recurse -Force }
if (Test-Path $docx) { Remove-Item -LiteralPath $docx -Force }

New-Item -ItemType Directory -Force -Path $work | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $work "_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $work "docProps") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $work "word") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $work "word\_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $work "word\media") | Out-Null

function Escape-Xml([string]$s) {
    if ($null -eq $s) { return "" }
    return [System.Security.SecurityElement]::Escape($s)
}

function Add-Paragraph([System.Text.StringBuilder]$sb, [string]$text, [string]$style = "Normal") {
    $escaped = Escape-Xml $text
    $styleXml = ""
    if ($style -ne "Normal") { $styleXml = "<w:pPr><w:pStyle w:val=`"$style`"/></w:pPr>" }
    [void]$sb.AppendLine("<w:p>$styleXml<w:r><w:t xml:space=`"preserve`">$escaped</w:t></w:r></w:p>")
}

function Add-Bullet([System.Text.StringBuilder]$sb, [string]$text) {
    $escaped = Escape-Xml $text
    [void]$sb.AppendLine("<w:p><w:pPr><w:ind w:left=`"720`" w:hanging=`"360`"/></w:pPr><w:r><w:t>• $escaped</w:t></w:r></w:p>")
}

function Add-PageBreak([System.Text.StringBuilder]$sb) {
    [void]$sb.AppendLine("<w:p><w:r><w:br w:type=`"page`"/></w:r></w:p>")
}

function Add-Table([System.Text.StringBuilder]$sb, [string[]]$headers, [object[][]]$rows) {
    [void]$sb.AppendLine("<w:tbl><w:tblPr><w:tblW w:w=`"5000`" w:type=`"pct`"/><w:tblBorders><w:top w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"94A3B8`"/><w:left w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"94A3B8`"/><w:bottom w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"94A3B8`"/><w:right w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"94A3B8`"/><w:insideH w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"CBD5E1`"/><w:insideV w:val=`"single`" w:sz=`"4`" w:space=`"0`" w:color=`"CBD5E1`"/></w:tblBorders></w:tblPr>")
    [void]$sb.AppendLine("<w:tr>")
    foreach ($h in $headers) {
        [void]$sb.AppendLine("<w:tc><w:tcPr><w:shd w:fill=`"0F2742`"/><w:tcW w:w=`"2400`" w:type=`"dxa`"/></w:tcPr><w:p><w:r><w:rPr><w:b/><w:color w:val=`"FFFFFF`"/></w:rPr><w:t>$(Escape-Xml $h)</w:t></w:r></w:p></w:tc>")
    }
    [void]$sb.AppendLine("</w:tr>")
    foreach ($row in $rows) {
        [void]$sb.AppendLine("<w:tr>")
        foreach ($cell in $row) {
            [void]$sb.AppendLine("<w:tc><w:p><w:r><w:t xml:space=`"preserve`">$(Escape-Xml ([string]$cell))</w:t></w:r></w:p></w:tc>")
        }
        [void]$sb.AppendLine("</w:tr>")
    }
    [void]$sb.AppendLine("</w:tbl>")
}

function Add-Image([System.Text.StringBuilder]$sb, [string]$path, [string]$caption, [ref]$rels, [ref]$imageIndex) {
    if (!(Test-Path $path)) {
        Add-Paragraph $sb "Imagen pendiente/no encontrada: $caption ($path)" "Normal"
        return
    }
    $idx = $imageIndex.Value
    $ext = [System.IO.Path]::GetExtension($path).ToLowerInvariant()
    if ($ext -eq "") { $ext = ".png" }
    $mediaName = "image$idx$ext"
    Copy-Item -LiteralPath $path -Destination (Join-Path $work "word\media\$mediaName") -Force
    $rid = "rIdImg$idx"
    $rels.Value += "<Relationship Id=`"$rid`" Type=`"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image`" Target=`"media/$mediaName`"/>`n"
    $cx = 5486400
    $cy = 3086100
    [void]$sb.AppendLine("<w:p><w:pPr><w:jc w:val=`"center`"/></w:pPr><w:r><w:drawing><wp:inline distT=`"0`" distB=`"0`" distL=`"0`" distR=`"0`"><wp:extent cx=`"$cx`" cy=`"$cy`"/><wp:effectExtent l=`"0`" t=`"0`" r=`"0`" b=`"0`"/><wp:docPr id=`"$idx`" name=`"Imagen $idx`"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=`"http://schemas.openxmlformats.org/drawingml/2006/main`" noChangeAspect=`"1`"/></wp:cNvGraphicFramePr><a:graphic xmlns:a=`"http://schemas.openxmlformats.org/drawingml/2006/main`"><a:graphicData uri=`"http://schemas.openxmlformats.org/drawingml/2006/picture`"><pic:pic xmlns:pic=`"http://schemas.openxmlformats.org/drawingml/2006/picture`"><pic:nvPicPr><pic:cNvPr id=`"$idx`" name=`"$mediaName`"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=`"$rid`"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=`"0`" y=`"0`"/><a:ext cx=`"$cx`" cy=`"$cy`"/></a:xfrm><a:prstGeom prst=`"rect`"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>")
    Add-Paragraph $sb "Figura $idx. $caption" "Caption"
    $imageIndex.Value = $idx + 1
}

$rels = ""
$imgIndex = 1
$doc = [System.Text.StringBuilder]::new()

[void]$doc.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>')
[void]$doc.AppendLine('<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><w:body>')

Add-Paragraph $doc "Sistema de Ventas con Microservicios" "Title"
Add-Paragraph $doc "Quarkus, Docker, Consul, API Gateway, JWT y Saga Pattern" "Subtitle"
Add-Paragraph $doc "Informe final basado en la implementacion real del proyecto"
Add-Paragraph $doc "Autor: Samuel Valencia"
Add-Paragraph $doc "Repositorios: FINAL-BACK-END y FINAL-FRONT-END"
Add-PageBreak $doc

Add-Paragraph $doc "1. Resumen Ejecutivo" "Heading1"
Add-Paragraph $doc "El proyecto implementa un sistema distribuido de ventas basado en microservicios. La arquitectura mantiene servicios independientes para autenticacion, clientes, productos y ventas, desplegados en Docker y registrados en Consul."
Add-Paragraph $doc "La operacion principal se realiza mediante Saga Orchestration. ms-ventas coordina validacion de cliente, validacion de producto, descuento de stock y registro de venta. Si ocurre un error despues del descuento, se ejecuta compensacion para restaurar stock."

Add-Paragraph $doc "2. Descripcion del Problema" "Heading1"
Add-Paragraph $doc "En microservicios cada servicio administra su responsabilidad y datos. Esto mejora la separacion, pero complica la consistencia distribuida. En una venta intervienen cliente, producto, stock y ventas; por eso se implementa Saga Pattern."
Add-Table $doc @("Paso","Servicio","Responsabilidad") @(
    @("1","ms-ventas","Inicia y coordina la Saga"),
    @("2","ms-cliente","Valida que el cliente exista y este activo"),
    @("3","ms-producto","Valida producto y stock disponible"),
    @("4","ms-producto","Descuenta stock"),
    @("5","ms-ventas","Registra la venta"),
    @("Compensacion","ms-ventas + ms-producto","Restaura stock ante error posterior al descuento")
)

Add-Paragraph $doc "3. Justificacion de Decisiones Tecnicas" "Heading1"
Add-Table $doc @("Decision","Justificacion") @(
    @("Mantener Quarkus","El proyecto ya estaba construido en Quarkus y no se cambio la tecnologia base."),
    @("REST Client","Permite comunicacion sincrona sin Kafka ni RabbitMQ."),
    @("Saga Orchestration","ms-ventas centraliza el flujo y ejecuta compensaciones."),
    @("Consul","Permite service discovery y registro de instancias."),
    @("API Gateway","Centraliza acceso externo."),
    @("JWT","Protege endpoints y diferencia roles."),
    @("No eliminar clientes","Mantiene integridad con usuarios y ventas."),
    @("Eliminar productos solo sin ventas","Mantiene trazabilidad historica.")
)

Add-Paragraph $doc "4. Objetivos" "Heading1"
Add-Paragraph $doc "Objetivo general: Implementar un sistema de ventas distribuido con microservicios en Quarkus, incorporando Saga Pattern para mantener consistencia de stock y ventas ante fallos."
Add-Bullet $doc "Implementar autenticacion con JWT."
Add-Bullet $doc "Centralizar acceso mediante API Gateway."
Add-Bullet $doc "Registrar servicios en Consul."
Add-Bullet $doc "Implementar CRUD de clientes y productos."
Add-Bullet $doc "Implementar venta coordinada con Saga Orchestration."
Add-Bullet $doc "Registrar Saga logs para auditoria."

Add-Paragraph $doc "5. Requerimientos Funcionales y No Funcionales" "Heading1"
Add-Table $doc @("Tipo","Requerimiento","Estado") @(
    @("Funcional","Login con JWT","Implementado"),
    @("Funcional","CRUD clientes seguro","Implementado"),
    @("Funcional","CRUD productos con eliminacion controlada","Implementado"),
    @("Funcional","Venta Saga exitosa","Implementado"),
    @("Funcional","Compensacion de stock","Implementado"),
    @("No funcional","Service discovery con Consul","Implementado"),
    @("No funcional","Resiliencia con Circuit Breaker/Fallback/Timeout","Implementado"),
    @("No funcional","Trazabilidad con Saga logs","Implementado")
)

Add-Paragraph $doc "6. Casos de Uso e Historias de Usuario" "Heading1"
Add-Table $doc @("Caso","Actor","Descripcion") @(
    @("CU-01 Login","Administrador / Cliente","Iniciar sesion y recibir JWT."),
    @("CU-02 Gestionar clientes","Administrador","Listar, actualizar, activar y desactivar clientes."),
    @("CU-03 Gestionar productos","Administrador","Crear, listar, actualizar y eliminar productos permitidos."),
    @("CU-04 Comprar","Cliente","Comprar mediante Saga."),
    @("CU-05 Auditar Saga","Administrador","Revisar ventas fallidas y compensaciones.")
)

Add-Paragraph $doc "7. Diagramas C4" "Heading1"
Add-Paragraph $doc "Nivel 1 - Contexto: Cliente y Administrador interactuan con Saga Store. El sistema se versiona en GitHub."
Add-Paragraph $doc "Nivel 2 - Contenedores: Frontend Angular consume api-gateway; api-gateway enruta a ms-auth, ms-cliente, ms-producto y ms-ventas. Consul registra servicios."
Add-Paragraph $doc "Nivel 3 - Componentes: ms-ventas contiene VentaController, VentaServiceImpl, SagaLogServiceImpl, repositorios y REST Clients."
Add-Paragraph $doc "Nivel 4 - Codigo: se usan anotaciones @CircuitBreaker, @Fallback y @Timeout en llamadas remotas."

Add-Paragraph $doc "8. API REST Documentada" "Heading1"
Add-Table $doc @("Modulo","Metodo","Ruta","Descripcion") @(
    @("Auth","POST","/api/auth/login","Login JWT"),
    @("Clientes","GET","/api/clientes","Listar clientes"),
    @("Clientes","PUT","/api/clientes/{id}/activar","Activar cliente"),
    @("Clientes","PUT","/api/clientes/{id}/desactivar","Desactivar cliente"),
    @("Productos","POST","/api/productos","Crear producto"),
    @("Productos","GET","/api/productos","Listar productos"),
    @("Productos","DELETE","/api/productos/{id}","Eliminar si no tiene ventas"),
    @("Ventas","GET","/api/ventas","Listar ventas"),
    @("Saga","POST","/api/ventas/saga","Ejecutar Saga"),
    @("Saga","GET","/api/ventas/saga-logs","Auditoria Saga")
)

Add-Paragraph $doc "9. Evidencias" "Heading1"
$images = @(
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-d549c25f-e989-4c96-a9bc-4601bc3d6fa1.png", "Docker Desktop con contenedores del proyecto activos"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-3c55917f-27b9-42f9-b334-c4af2c0063f5.png", "Servicios registrados en Consul"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-4cfe9b34-768f-4d21-b95b-2748b0423753.png", "ms-producto con dos instancias para balanceo"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-a9d2ee66-45eb-4c9b-9ea5-38c46bafe482.png", "Login admin por API Gateway con JWT"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-3ba089c2-47e6-4b1b-b3d2-b2ba4f2d3db4.png", "Listado de clientes por gateway"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-74ac4de1-5867-4603-91f8-3be273168d0f.png", "Creacion de producto por gateway"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-cee15dd6-e585-43a2-bf3d-35f4a8cda0ed.png", "Error al eliminar producto con ventas"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-5b0e078c-04c1-4f86-99d9-77aad4abb599.png", "Venta Saga exitosa"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-005e609e-9217-46bd-b873-347d1f54ef05.png", "Error por stock insuficiente"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-df247cbc-fa45-4922-a5df-872ccc513b36.png", "Error por cliente inactivo"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-4c9b47ca-7a37-4fb0-b89c-fd956b4225af.png", "Stock antes de compensacion"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-3bcc9ec4-d7be-4b8a-a3a5-4c6f326d70d1.png", "Error simulado despues del descuento"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-c596542c-22d0-497e-b61d-7c2792331c85.png", "Stock restaurado por compensacion"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-261a8b3c-153d-43e2-9c43-b3b99a6f3fb4.png", "Saga logs desde Postman"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-1a383005-090c-4da5-9c19-ef7ef1d9c0d3.png", "Codigo con Circuit Breaker, Fallback y Timeout"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-cba9341f-20ed-4e03-8e37-e95102397f4a.png", "Login de cliente en frontend"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-e477f53b-ff9c-4f8f-9230-bb34f47d3367.png", "Login de administrador en frontend"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-efb205e2-e76d-4bc7-8162-bf0a00e021f6.png", "Panel administrativo Angular"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-e4075fac-1a8e-4496-9871-fe55a6ac85af.png", "Gestion de productos en frontend"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-9ad65a07-77af-4f42-b08f-114289e01a3d.png", "Saga logs en frontend"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-eea299df-1ce3-4fcc-b359-9e80c40d7e59.png", "Tienda y carrito"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-a1455cd4-6339-4ea5-91cd-156e1ffb7ca9.png", "Historial de compras"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-c1678d88-d6ab-4300-ad1b-502308b90d50.png", "Repositorios GitHub"),
    @("C:\Users\LENOVO\AppData\Local\Temp\codex-clipboard-7f1f9b1f-04c7-4b4c-8a85-bd1bc3f32acd.png", "Repositorio backend")
)

foreach ($img in $images) {
    Add-Image $doc $img[0] $img[1] ([ref]$rels) ([ref]$imgIndex)
}

Add-Paragraph $doc "10. Manual Tecnico" "Heading1"
Add-Paragraph $doc "Para ejecutar el backend se usa Docker Compose desde la raiz del proyecto:"
Add-Paragraph $doc "docker compose up -d --build"
Add-Paragraph $doc "Para ejecutar el frontend Angular:"
Add-Paragraph $doc "npm install"
Add-Paragraph $doc "npm start"

Add-Paragraph $doc "11. Manual de Usuario" "Heading1"
Add-Bullet $doc "El administrador ingresa por el acceso Admin y gestiona clientes, productos, ventas y Saga logs."
Add-Bullet $doc "El cliente inicia sesion, agrega productos al carrito y compra mediante Saga."
Add-Bullet $doc "Los errores funcionales se muestran como mensajes controlados."

Add-Paragraph $doc "12. Conclusiones" "Heading1"
Add-Paragraph $doc "El sistema cumple con una arquitectura distribuida en Quarkus, usando Docker, Consul, API Gateway y Saga Pattern. La implementacion conserva trazabilidad, maneja errores funcionales, protege operaciones con JWT y restaura stock mediante compensacion cuando ocurre un fallo despues del descuento."

[void]$doc.AppendLine('<w:sectPr><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1008" w:right="1008" w:bottom="1008" w:left="1008"/></w:sectPr></w:body></w:document>')

$styles = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:rPr><w:b/><w:color w:val="0F172A"/><w:sz w:val="48"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:rPr><w:b/><w:color w:val="0E7490"/><w:sz w:val="28"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:rPr><w:b/><w:color w:val="12355B"/><w:sz w:val="32"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Caption"><w:name w:val="caption"/><w:rPr><w:i/><w:color w:val="475569"/><w:sz w:val="20"/></w:rPr></w:style>
</w:styles>
'@

$contentTypes = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Default Extension="jpeg" ContentType="image/jpeg"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
'@

$rootRels = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
'@

$docRels = "<?xml version=`"1.0`" encoding=`"UTF-8`" standalone=`"yes`"?><Relationships xmlns=`"http://schemas.openxmlformats.org/package/2006/relationships`"><Relationship Id=`"rIdStyles`" Type=`"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles`" Target=`"styles.xml`"/>$rels</Relationships>"
$core = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>Informe Final Microservicios</dc:title><dc:creator>Samuel Valencia</dc:creator></cp:coreProperties>'
$app = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"><Application>Codex</Application></Properties>'

Set-Content -LiteralPath (Join-Path $work "[Content_Types].xml") -Value $contentTypes -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "_rels\.rels") -Value $rootRels -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "word\document.xml") -Value $doc.ToString() -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "word\styles.xml") -Value $styles -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "word\_rels\document.xml.rels") -Value $docRels -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "docProps\core.xml") -Value $core -Encoding UTF8
Set-Content -LiteralPath (Join-Path $work "docProps\app.xml") -Value $app -Encoding UTF8

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($work, $docx)
Remove-Item -LiteralPath $work -Recurse -Force
Write-Host "Documento generado: $docx"
