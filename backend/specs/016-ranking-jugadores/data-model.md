# Data Model

## Entidades existentes

### Jugador

- `id`, `externalId`, `name`, `team`, `league`, `position`.
- Fuente: registros ya persistidos por el scraping existente.
- El ranking solo consulta jugadores; no los inserta, registra ni modifica.
- `PlayerEntity` no tiene campo `activo`; en esta feature no se agregan estado ni ciclo de vida nuevos. La elegibilidad se determina sobre los registros del catalogo y las cinco ligas incluidas por el scraper.

### Liga

- `externalId`, `name`, `country`.
- Las cinco ligas elegibles tienen external ID `{2,3,4,5,22}`.

### Usuario

- El superusuario se identifica por el nombre configurado `superuser`.
- `UserEntity` actual no tiene campo `activo`; las cotizaciones personalizadas se filtran por cotizacion vigente y configuracion ACTUAL existente.

### Cotizacion vigente

- `isCurrent=true`, usuario, jugador, valor unitario y estrategia.
- Solo cotizaciones vigentes y configuracion ACTUAL participan.

### Configuracion de estrategia

- Estado `ACTUAL`; usuario y tipo deben coincidir con la cotizacion.

### Tenencia

- `tokens`: cantidad actual poseida.
- `valorUnitario`: cotizacion vigente por usuario/jugador.
- La tenencia del superusuario aporta la base; las restantes aportan al desempate.

## Modelos de dominio

### RankingFiltros

- `liga`, `equipo`, `posicion`: opcionales, trim, longitud 1-100 y sin caracteres de control.
- `pagina`: entero positivo.
- `limite`: entero entre 1 y 100.

### RankingJugador

- `posicion`, `playerId`, `nombre`, `equipo`, `liga`, `posicionEnCancha`, `valorRepresentativo`, `origen`.
- Origen permitido: `ESTRATEGIA_A`, `ESTRATEGIA_B` o `Cotizacion base`.
- Valor no negativo.

### RankingPagina

- `items`, `total`, `page`, `limit`.

## Indices

- `idx_players_league_team_position` sobre `(league_id, team_id, position)`.
- `idx_players_name_external_id` sobre `(name, external_id)`.
- `idx_valuaciones_current_player_user_strategy` sobre `(is_current, jugador_id, usuario_id, estrategia)`.
- `idx_estrategia_status_user_type` sobre `(estado, usuario_id, tipoestrategia)`.
- `idx_tenencias_player_user_tokens` sobre `(jugador_id, usuario_id, tokens)`.
PostgreSQL conserva el indice parcial unico existente `valuaciones(usuario_id, jugador_id) WHERE is_current=true`.

## Carga de catalogo

- `POST /quotes/scrape` es la ruta existente para poblar/actualizar los jugadores y cotizaciones base persistidos.
- El contrato central `docs/openapi.yaml` declara `operationId: scrape`, sin seguridad (`security: []`), y referencia `#/components/parameters/NumberOfPlayers`.
- El query parameter en el contrato es `numberOfPlayers`, opcional, default `50`, minimo `1`; respuestas documentadas: 200 y 400.
- Esta funcionalidad no agrega operaciones de escritura de jugadores al endpoint de ranking.
