# Analise LiquidBounce nextgen x FDPClient 1.8.9

Data: 2026-06-14

Fontes locais analisadas:

- `C:\Users\yzy\Downloads\LiquidBounce-nextgen`
- `C:\Users\yzy\IdeaProjects\FDPClient`

## Resumo executivo

O LiquidBounce nextgen analisado nao e uma base 1.8.9 nativa. Ele e um cliente Fabric moderno, em Kotlin/Java, voltado para Minecraft `>1.21.11 <26.2`, com Java/JDK 25, Fabric Loader, Fabric API, ViaFabricPlus, MCEF, Svelte/Vite para interface e varias dependencias modernas.

O suporte a servidores 1.8 nele aparece principalmente via ViaFabricPlus, ou seja, compatibilidade de protocolo para conectar em servidores 1.8 enquanto o cliente roda em uma versao moderna. Para o FDPClient, que ja e nativo Forge 1.8.9 com Java 8 e MCP, a estrategia correta e portar conceitos, algoritmos, listas de modos, organizacao de valores, testes e UX. Copiar classes diretamente exigiria reescrita forte por causa de diferencas de API, mapeamentos, eventos, pacotes, renderizacao, inventario e rede.

Pontos mais valiosos para trazer ao FDP:

- Modelo de `InventoryCleaner` por facetas e plano de limpeza.
- Arquitetura de comandos com subcomandos, autocomplete, paginacao e dominios separados.
- Melhorias de config por grupos de valores, escolhas, toggles compostos e serializacao mais robusta.
- Testes unitarios para rotacao, scaffold, inventario, geometria, busca e utilitarios.
- DebugRecorder, PacketLogger e ferramentas de diagnostico locais.
- Alguns modulos utilitarios e visuais que o FDP nao tem ou tem de forma menos completa.
- Ideias pontuais de modos 1.8, principalmente NoSlow Grim 2.3.64, Fly Vulcan 286 1.8 e tecnicas de scaffold, sempre reimplementadas para Forge 1.8.9.

Pontos que nao recomendo portar:

- ServerCrasher e exploits destrutivos.
- Dupe e payloads feitos para derrubar ou abusar servidor.
- Stack completa Svelte/MCEF/ViaFabricPlus como dependencia direta.
- Funcionalidades modernas sem equivalente real na 1.8.9, como Elytra, Offhand, Shield, Crossbow, Mace, WindCharge, Bundle e mecanicas de blocos/itens modernos.

## Base tecnica

### LiquidBounce nextgen

Arquivos principais:

- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `src/main/kotlin`
- `src/main/java`
- `src-theme`
- `src/test/kotlin`

Caracteristicas:

- Fabric Loom.
- Fabric API.
- Kotlin 2.4.0.
- Minecraft moderno, configurado em torno de `26.1.2`.
- `mod_version=0.38.1`.
- `mod_mc_version=>1.21.11 <26.2`.
- ViaFabricPlus para protocolos antigos, incluindo 1.8.
- MCEF e backend de browser para interfaces web.
- GraalVM Polyglot para scripts JavaScript.
- DJL/PyTorch em dependencias de machine learning.
- OkHttp, Discord IPC, JCEF/MCEF e bibliotecas modernas.

### FDPClient

Arquivos principais:

- `build.gradle`
- `gradle.properties`
- `src/main/java`
- `src/main/kotlin`
- `src/main/resources`

Caracteristicas:

- ForgeGradle 2.1.
- Minecraft Forge `1.8.9-11.15.1.2318-1.8.9`.
- MCP mappings `stable_22`.
- Java 8.
- Mixin 0.7.11.
- Kotlin stdlib/coroutines.
- DiscordIPC, OkHttp, Elixir, FlatLaf, XChart e stack compativel com 1.8.9.

Impacto: o FDP nao deve importar a base Fabric nextgen como dependencia. O port correto e por reimplementacao em cima dos eventos, pacotes e classes MCP/Forge 1.8.9 do FDP.

## Cobertura do levantamento

Foram inventariados:

- Estrutura de diretorios.
- Build e dependencias.
- Modulos por categoria.
- Modos de Fly, Speed, LongJump, NoFall, Velocity, Criticals, Disabler, Phase, ServerCrasher, NoSlow, NoWeb, LiquidWalk, Spider e Scaffold.
- Comandos.
- HUD/theme/web UI.
- Config, valores e serializacao.
- Script API.
- APIs remotas.
- Testes.
- Pontos especificos de compatibilidade 1.8.
- Diferencas entre modulos existentes no FDP e modulos do nextgen.

Observacao: esta e uma analise completa em nivel de produto, arquitetura e inventario funcional. Antes de portar qualquer item especifico, ainda e necessario revisar o arquivo alvo linha a linha para adaptar chamadas de rede, eventos, nomes MCP e comportamento 1.8.9.

## Suporte 1.8 no nextgen

Arquivos centrais:

- `src/main/java/net/ccbluex/liquidbounce/utils/client/vfp/VfpCompatibility.java`
- `src/main/java/net/ccbluex/liquidbounce/utils/client/vfp/VfpCompatibility1_8.java`
- `src/main/kotlin/net/ccbluex/liquidbounce/utils/client/ProtocolUtil.kt`

Funcoes e ideias relevantes:

- Checagem do protocolo selecionado no ViaFabricPlus.
- Helpers como `isEqual1_8`, `isOlderThanOrEqual1_8`, `isOlderThanOrEqual1_7_10` e variantes para versoes modernas.
- Envio de pacotes 1.8 especificos para update de placa e input do jogador.
- Uso de gates por protocolo em modulos que so fazem sentido em servidores 1.8.

Como aplicar no FDP:

- Como o FDP ja roda em 1.8.9, ViaFabricPlus nao deve ser portado.
- Vale portar o conceito de "compatibility gates" para separar modos que dependem de ambiente, servidor ou anticheat.
- Vale documentar modos com requisitos claros, por exemplo "somente 1.8 server-side".
- Helpers de pacotes devem ser reescritos usando `C03PacketPlayer`, `C0APacketAnimation`, `C08PacketPlayerBlockPlacement`, `C0EPacketClickWindow`, `C12PacketUpdateSign` e classes MCP equivalentes.

Usos 1.8 explicitos encontrados:

- `DisablerMiniblox.kt`: exige ViaFabricPlus e protocolo 1.8, usa input 1.8.
- `TranslationSign18Exploit.kt`: exploit de placa 1.8. Nao recomendado para o FDP.
- `FlyVulcan286MC18.kt`: modo `Vulcan286-18`, marcado como apenas para servidores 1.8.
- `FlyVulcan286Teleport.kt`: tambem com requisito de 1.8 server-side.
- `NoSlowSharedGrim2364MC18.kt`: modo `Grim2364-1.8`, usa troca de slot.

## Inventario de modulos do LiquidBounce nextgen

Total identificado: 234 modulos.

### Combat, 28

Aimbot, AutoArmor, AutoBow, AutoClicker, AutoDodge, AutoLeave, AutoPearl, AutoRod, AutoShoot, AutoWeapon, Backtrack, Criticals, CrystalAura, DroneControl, ElytraTarget, FakeLag, Hitbox, KeepSprint, KillAura, MaceKill, NoMissCooldown, ProjectileAimbot, SuperKnockback, SwordBlock, TickBase, TimerRange, TpAura, Velocity.

### Exploit, 25

AbortBreaking, AntiHunger, AntiReducedDebugInfo, BookBot, ClickTp, Damage, Disabler, Dupe, GhostHand, Kick, MoreCarry, MultiActions, NameCollector, NoPitchLimit, Phase, PingSpoof, Plugins, PortalMenu, ResetVL, ServerCrasher, SleepWalker, Teleport, TimeShift, VehicleOneHit, YggdrasilSignatureFix.

### Fun, 7

DankBobbing, Derp, HandDerp, Notebot, SkinDerp, Twerk, Vomit.

### Misc, 20

AntiBot, AntiCheatDetect, AntiStaff, AutoAccount, AutoChatGame, AutoConfig, DebugRecorder, EasyPearl, FlagCheck, GUICloser, ItemScroller, Macros, MiddleClickAction, NameProtect, Notifier, PacketLogger, Spammer, TargetLock, Teams, TextFieldProtect.

### Movement, 38

AirJump, Anchor, AntiBounce, AntiLevitation, AvoidHazards, BlockBounce, BlockWalk, Clip, ElytraFly, ElytraRecast, EntityControl, ExtendedFirework, Fly, Freeze, HighJump, InventoryMove, LiquidWalk, LongJump, NoClip, NoJumpDelay, NoPose, NoPush, NoSlow, NoWeb, Parkour, ReverseStep, SafeWalk, SnapTap, Sneak, Speed, Spider, Sprint, Step, Strafe, TargetStrafe, TerrainSpeed, VehicleBoost, VehicleControl.

### Player, 30

AntiAFK, AntiExploit, AntiVoid, AutoBreak, AutoCrafter, AutoFish, AutoQueue, AutoRespawn, AutoShop, AutoWalk, AutoWindCharge, Blink, ChestCleaner, ChestStealer, Eagle, ElytraSwap, FastExp, FastUse, InventoryCleaner, NoBlockInteract, NoEntityInteract, NoFall, NoRotateSet, NoSlotSet, Offhand, PotionSpoof, Reach, Replenish, ReportHelper, SmartEat.

### Render, 59

Animations, AntiBlind, Aspect, AutoF5, BedPlates, BetterChat, BetterInventory, BetterTab, BetterTitle, BlockESP, BlockOutline, Breadcrumbs, CameraClip, Chams, ClickGUI, CombineMobs, Crosshair, CrystalView, CustomAmbience, DamageParticles, Debug, ESP, FreeCam, FreeLook, FullBright, Hats, HitFX, HoleESP, HUD, ItemChams, ItemESP, ItemTags, JumpEffect, LogoffSpot, MobOwners, MurderMystery, Nametags, NewChunks, NoBob, NoFOV, NoHurtCam, NoSwing, Particles, ProphuntESP, ProtectionZones, QuickPerspectiveSwap, Radar, Rotations, SilentHotbar, SkinChanger, SmoothCamera, StorageESP, TNTTimer, Tracers, Trajectories, TrueSight, VoidESP, XRay, Zoom.

### World, 27

AirPlace, AutoBuild, AutoDisable, AutoFarm, AutoMobHeal, AutoTool, AutoTrap, BedDefender, BlockIn, BlockTrap, Extinguish, FastBreak, FastPlace, Fucker, HoleFiller, InventoryTracker, LiquidFiller, LiquidPlace, NoInterpolation, NoSlowBreak, Nuker, PacketMine, ProjectilePuncher, Scaffold, StrongholdFinder, Surround, Timer.

## Inventario de modulos do FDPClient

Total identificado: 189 modulos.

### Client, 18

Animations, AntiBot, BrandSpoofer, CapeManager, ChatControl, ClickGUI, DiscordRPC, GameDetector, HUD, HudDesigner, IRC, Rotations, SnakeGame, Spotify, TabGUI, Target, Teams, Wings.

### Combat, 25

Aimbot, ArmorFilter, AutoArmor, AutoBlock, AutoBow, AutoClicker, AutoProjectile, AutoRod, AutoWeapon, Backtrack, Criticals, FakeLag, FastBow, FightBot, ForwardTrack, HitBox, HitSelect, Ignite, KeepSprint, KillAura, ProjectileAimbot, SuperKnockback, TickBase, TimerRange, Velocity.

### Exploit, 22

AbortBreaking, AntiExploit, AntiHunger, Damage, Disabler, ForceUnicodeChat, Ghost, GhostHand, GuiClicker, ItemTeleport, LightningDetect, MultiActions, NoPitchLimit, PacketDebugger, Phase, PingFix, PingSpoof, Plugins, ResourcePackSpoof, ServerCrasher, ServerLag, Teleport.

### Movement, 31

AirJump, AntiBounce, AntiVoid, AutoWalk, FastAccel, FastBreak, FastClimb, Flight, HighJump, InstantStop, InvMove, Jesus, LongJump, MoveHelper, NoClip, NoFluid, NoJumpDelay, NoSlow, NoWeb, Parkour, SafeWalk, SnapTap, Sneak, Speed, Spider, Sprint, Step, Strafe, TargetStrafe, Timer, WallClimb.

### Other, 22

AntiCheatDetector, AutoAccount, AutoAddStaff, AutoDisable, BedDefender, ChestAura, ChestStealer, CivBreak, FakePlayer, FastPlace, FlagCheck, Fucker, MurderDetector, NoRotateSet, NoSlotSet, Notifier, Nuker, OverrideRaycast, RemoveEffect, Spammer, StaffDetector, UnlimitedValues.

### Player, 24

AntiAFK, AntiFireball, AutoBreak, AutoFish, AutoPlay, AutoPot, AutoRespawn, AutoSoup, AutoTool, AvoidHazards, Blink, DelayRemover, Eagle, FastUse, Gapple, InventoryCleaner, KeepAlive, MidClick, NoFall, PotionSpoof, Reach, Refill, Regen, Scaffold.

### Visual, 47

Ambience, AntiBlind, BedPlates, BedProtectionESP, BlockESP, BlockOverlay, Breadcrumbs, CameraView, Chams, ChineseHat, CombatVisuals, CustomModel, DamageParticle, DashTrail, ESP, ESP2D, FireFlies, FreeCam, FreeLook, Fullbright, Glint, HealthWarn, HitBubbles, ItemESP, ItemPhysics, JumpCircle, KeepTabList, LineGlyphs, NameProtect, NameTags, NoBob, NoBooks, NoFOV, NoHurtCam, NoRender, NoSwing, PointerESP, Projectiles, ProphuntESP, SilentHotbar, StorageESP, TNTESP, TNTTimer, TNTTrails, Tracers, TrueSight, XRay.

## Diferencas brutas de modulos

Esta comparacao e por nome exato. Existem equivalencias com nomes diferentes, como `Fly` x `Flight`, `InventoryMove` x `InvMove`, `LiquidWalk` x `Jesus`, `Nametags` x `NameTags`, `MiddleClickAction` x `MidClick`.

### Presentes no nextgen e ausentes por nome no FDP

AirPlace, Anchor, AntiCheatDetect, AntiLevitation, AntiReducedDebugInfo, AntiStaff, Aspect, AutoBuild, AutoChatGame, AutoConfig, AutoCrafter, AutoDodge, AutoF5, AutoFarm, AutoLeave, AutoMobHeal, AutoPearl, AutoQueue, AutoShoot, AutoShop, AutoTrap, AutoWindCharge, BetterChat, BetterInventory, BetterTab, BetterTitle, BlockBounce, BlockIn, BlockOutline, BlockTrap, BlockWalk, BookBot, CameraClip, ChestCleaner, ClickTp, Clip, CombineMobs, Crosshair, CrystalAura, CrystalView, CustomAmbience, DamageParticles, DankBobbing, Debug, DebugRecorder, Derp, DroneControl, Dupe, EasyPearl, ElytraFly, ElytraRecast, ElytraSwap, ElytraTarget, EntityControl, ExtendedFirework, Extinguish, FastExp, Fly, Freeze, GUICloser, HandDerp, Hats, HitFX, HoleESP, HoleFiller, InventoryMove, InventoryTracker, ItemChams, ItemScroller, ItemTags, JumpEffect, Kick, LiquidFiller, LiquidPlace, LiquidWalk, LogoffSpot, MaceKill, Macros, MiddleClickAction, MobOwners, MoreCarry, MurderMystery, NameCollector, NewChunks, NoBlockInteract, NoEntityInteract, NoInterpolation, NoMissCooldown, NoPose, NoPush, NoSlowBreak, Notebot, Offhand, PacketLogger, PacketMine, Particles, PortalMenu, ProjectilePuncher, ProtectionZones, QuickPerspectiveSwap, Radar, Replenish, ReportHelper, ResetVL, ReverseStep, SkinChanger, SkinDerp, SleepWalker, SmartEat, SmoothCamera, StrongholdFinder, Surround, SwordBlock, TargetLock, TerrainSpeed, TextFieldProtect, TimeShift, TpAura, Trajectories, Twerk, VehicleBoost, VehicleControl, VehicleOneHit, VoidESP, Vomit, YggdrasilSignatureFix, Zoom.

### Presentes no FDP e ausentes por nome no nextgen

Ambience, AntiCheatDetector, AntiFireball, ArmorFilter, AutoAddStaff, AutoBlock, AutoPlay, AutoPot, AutoProjectile, AutoSoup, BedProtectionESP, BlockOverlay, BrandSpoofer, CameraView, CapeManager, ChatControl, ChestAura, ChineseHat, CivBreak, CombatVisuals, CustomModel, DamageParticle, DashTrail, DelayRemover, DiscordRPC, ESP2D, FakePlayer, FastAccel, FastBow, FastClimb, FightBot, FireFlies, Flight, ForceUnicodeChat, ForwardTrack, GameDetector, Gapple, Ghost, Glint, GuiClicker, HealthWarn, HitBubbles, HitSelect, HudDesigner, Ignite, InstantStop, InvMove, IRC, ItemPhysics, ItemTeleport, Jesus, JumpCircle, KeepAlive, KeepTabList, LightningDetect, LineGlyphs, MidClick, MoveHelper, MurderDetector, NoBooks, NoFluid, NoRender, OverrideRaycast, PacketDebugger, PingFix, PointerESP, Projectiles, Refill, Regen, RemoveEffect, ResourcePackSpoof, ServerLag, SnakeGame, Spotify, StaffDetector, TabGUI, Target, TNTESP, TNTTrails, UnlimitedValues, WallClimb, Wings.

## Modos e detalhes relevantes do nextgen

### Fly

Modos identificados:

AirWalk, Creative, Custom, Enderpearl, Explosion, Fireball, Grim2373Jan15, Grim2859-V, HycraftDamage, Hypixel, HypixelFlat, Instant, Jetpack, Legit, NcpClip, OnEdge, Sentinel10thMar, Sentinel20thApr, Sentinel26thDec, Sentinel27thJan, Spartan524, Vanilla, VerusB3896Damage, VerusB3896Flat, Vulcan277, Vulcan286-113, Vulcan286-18, Vulcan286-Teleport-18.

Para FDP:

- FDP ja tem muitos modos de Flight e alguns provavelmente cobrem NCP, Verus, Vulcan, Hypixel e Vanilla.
- Valem revisao: `Vulcan286-18`, `Vulcan286-Teleport-18`, `OnEdge`, `Grim2373Jan15`, `Grim2859-V`.
- Modos com Elytra ou mecanicas modernas nao sao aplicaveis.

### Speed

Modos e arquivos relevantes:

BlocksMC, GrimCollide, HylexGround, SentinelDamage, Spartan-4.0.4.3, Spartan-4.0.4.3-FastFall, Custom, Generic, Intave14, Matrix7, NCP, Vulcan286, Vulcan288, VulcanGround286, HypixelBHop, HypixelLowHop, VerusB3882.

Para FDP:

- FDP ja tem Speed bem amplo.
- Valem revisao pontual de `GrimCollide`, `SentinelDamage`, `VulcanGround286` e organizacao de valores do `Custom`.
- Nao substituir os modos existentes em bloco.

### LongJump

Modos identificados:

Matrix-7.14.5-Flag, NoCheatPlusBoost, NoCheatPlusBow, Vulcan289.

Para FDP:

- FDP ja cobre NCP, Matrix, Verus, Vulcan e Hycraft.
- Pode valer comparar `Matrix-7.14.5-Flag` e `Vulcan289` como variantes, sem alterar defaults.

### NoFall

Modos e arquivos relevantes:

Falling, Landing, Blink, BlocksMC, Cancel, Constant, ForceJump, Grim2371-1.9+, Hypixel, HypixelPacket, MLG, Mount, NoGround, Packet, PacketJump, Rettungsplatform, Smart, Spartan524Flag, SpoofGround, Verus, Vulcan277, VulcanTP288.

Para FDP:

- FDP ja tem NoFall muito completo.
- Valem apenas revisoes de comportamento em `Smart`, `VulcanTP288` e organizacao por escolhas.
- Modos 1.9+ nao devem ser portados para 1.8.9 sem equivalencia.

### Velocity

Modos identificados:

AAC4.4.2, BlocksMC, Dexland, Grim2344-117, Grim2371, Hylex, Hypixel, Intave, JumpReset, Lag, Modify, Reduce, Reversal, Strafe.

Para FDP:

- FDP parece mais completo que o nextgen nesse ponto para 1.8.9.
- Vale comparar `JumpReset`, `Lag` e `Reversal` pela ergonomia.
- Nao vale trocar a implementacao inteira.

### Criticals

Modos identificados:

Blink, Jump, NoGround, Packet, Timer.

Para FDP:

- FDP ja tem Criticals.
- Pode valer comparar `Blink` e `Timer` se o FDP ainda nao tiver equivalentes bons.

### Disabler

Modos identificados:

AAC1.9.10, GrimSpectate, Hypixel, HypixelScaffold, LiveOverflow, MinelandFly, Miniblox, NoAction, SpigotSpam, SwingOrder, VanillaSpeed, VerusCombat, VerusExperimental, VerusScaffoldG, VoidTP, VulcanRiptide, VulcanScaffold.

Para FDP:

- FDP ja tem varios disablers, incluindo Miniblox, VulcanScaffold, Verus e Watchdog.
- Valem revisao: `SwingOrder`, `NoAction`, `HypixelScaffold`, `GrimSpectate`.
- Qualquer modo deve ficar opt-in e bem isolado, pois disablers sao sensiveis a regressao.

### NoSlow

Modos identificados:

AAC5, Blink, Grim2360, Grim2364-1.8, Grim2371, Intave14, Interact, InvalidHand, Jump, Release, Reuse, Switch.

Para FDP:

- Prioridade alta: analisar `Grim2364-1.8`.
- Prioridade media: `Switch`, `Reuse`, `Interact`.
- Modos com hand/offhand modernos precisam adaptacao ou descarte.

### Scaffold

Tecnicas:

Breezily, Expand, GodBridge, Normal.

Features:

Acceleration, AutoBlock, Blink, Ceiling, HeadHitter, Prediction, SpeedLimiter, SprintControl, Strafe, StrafeOnJump.

Para FDP:

- FDP ja tem Scaffold forte, incluindo Normal, Rewinside, Expand, Telly e GodBridge.
- Valem portar conceitos de `Prediction`, `SpeedLimiter`, `SprintControl`, `StrafeOnJump` e possivelmente organizacao de valores.
- Nao vale substituir o Scaffold completo sem testes, pois e um dos modulos de maior risco.

### World

Itens interessantes:

AirPlace, AutoBuild, AutoTrap, BlockIn, BlockTrap, Extinguish, HoleFiller, InventoryTracker, LiquidFiller, LiquidPlace, NoInterpolation, NoSlowBreak, PacketMine, ProjectilePuncher, StrongholdFinder, Surround.

Para FDP:

- `StrongholdFinder` e `ProjectilePuncher` sao bons candidatos isolados.
- `BlockIn`, `BlockTrap`, `AutoTrap`, `Surround` dependem do meta PvP e devem ser simplificados para 1.8.
- `PacketMine` precisa reimplementacao 1.8 e teste de compatibilidade com servidores.

## Comandos

### LiquidBounce nextgen

Possui cerca de 67 arquivos de comandos e subcomandos. Grupos observados:

- Cliente: bind, binds, clear, config, debug, friend, help, hide, localconfig, panic, script, targets, toggle, value.
- Subcomandos de client: info, browser, integration, language, theme, appearance, prefix, destruct, account, cosmetics, config.
- Marketplace: search, subscribe, unsubscribe, update, item, revisions, create, edit, delete, list, upload.
- In-game: center, coordinates, ping, remoteview, say, serverinfo, tps, username.
- Creative: enchant, give, rename, skull, stack.
- Modulos: autoaccount, autodisable, invsee, xray, teleport, vclip, playerteleport.
- Translate: translate, autotranslate, language codes.
- FakePlayer e utilitarios relacionados.

### FDPClient

Comandos identificados: AddAll, AutoDisable, Bind, Binds, ChatAdmin, ChatToken, Clip, Connect, Damage, Focus, Friend, Give, Help, Hurt, IRCChat, LocalSettings, LocalThemes, Macro, PacketDebugger, Panic, Ping, Plugins, Prefix, PrivateChat, Reload, RemoteView, Rename, Say, ScriptManager, ServerInfo, Settings, Teleport, Toggle, Tps, Username, Xray.

Recomendacao:

- Portar a arquitetura, nao os comandos em massa.
- Criar um command builder com argumentos tipados, subcomandos, autocomplete e mensagens de erro padronizadas.
- Migrar comandos aos poucos, mantendo aliases atuais para nao quebrar usuarios.

## HUD, tema e interface

### LiquidBounce nextgen

O nextgen possui `src-theme` com Svelte, Vite e TypeScript. A interface inclui:

- ClickGUI.
- HUD editor.
- Browser UI.
- Inventory UI.
- Main menu, singleplayer, multiplayer, proxy manager, alt manager e tela de desconexao.
- Componentes de setting para boolean, escolha, cor, rangos, key bind, texto, vetores e listas.
- Backend de integracao com REST/WebSocket e armazenamento persistente.

Componentes HUD observados:

armoritems, arraylist, blockcounter, craftinginventory, effects, enderchestinventory, hotbar, image, inventory, inventorystatistics, keybinds, keystrokes, notifications, scoreboard, tabgui, taco, targethud, text, watermark.

### FDPClient

O FDP ja possui ClickGUI/HUD nativos, assets, shaders, estilos proprios e varias telas. Nao usa MCEF/Svelte como camada principal.

Recomendacao:

- Nao portar MCEF/Svelte agora.
- Portar ideias de UX: busca de modulos, listas virtuais, edicao de valores mais clara, presets, agrupamento de settings, tooltips e componentes de HUD configuraveis.
- Adicionar `Zoom`, `Crosshair`, `BlockOutline`, `QuickPerspectiveSwap`, `LogoffSpot`, `Radar`, `ItemTags` e `BetterTab` como candidatos visuais.

## Config e valores

### LiquidBounce nextgen

Componentes encontrados:

- `ConfigSystem`
- `AutoConfig`
- metadata/includes
- adapters e serializers Gson
- `BindValue`
- `CurveValue`
- `FileValue`
- `RangedValue`
- `Vec3Value`
- `ModeValueGroup`
- `ToggleableValueGroup`
- `ChoiceList`
- `MultiChoiceList`
- `AutoCompletionProvider`
- `RefreshableRangeValue`
- `TextureMode`

### FDPClient

O FDP tem sistema de arquivos/configs proprio para contas, ClickGUI, tema, amigos, HUD, modulos, valores e macros.

Recomendacao:

- Nao trocar o config system inteiro de uma vez.
- Portar `ModeValueGroup` e `ToggleableValueGroup` como conceito.
- Melhorar serializacao de valores complexos.
- Adicionar validacao de config e fallback seguro para valores quebrados.
- Escrever testes para carregamento/salvamento de configs antes de migrar.

## Script API

### LiquidBounce nextgen

Usa GraalVM Polyglot JS com:

- `PolyglotScript`
- `ScriptManager`
- bindings para async, bloco, cliente, interacao, item, movimento, rede, primitives, reflection, rotation e thread
- modulos, settings e comandos de script

### FDPClient

Possui sistema de scripts legado e remapper.

Recomendacao:

- Nao portar GraalVM diretamente para Java 8 sem prova de compatibilidade e custo.
- Melhorar a API atual por partes: bindings tipados, documentacao interna, lifecycle claro e isolamento de erros.
- Portar a ideia de scripts criarem comandos/settings de forma mais padronizada.

## APIs e servicos

O nextgen possui camada de API para:

- auth/OAuth/PKCE
- update/autosettings
- cosmetics/cape
- marketplace
- user
- Mojang
- skin
- translate providers
- OpenAI API
- IP info

Para FDP:

- Marketplace e cloud config so valem se houver backend proprio.
- Translate/autotranslate pode ser util se ficar opcional.
- Update/autosettings pode inspirar um sistema de presets por servidor.
- Auth/OAuth nao deve ser copiado sem necessidade real.

## Testes

O nextgen tem testes em `src/test/kotlin` para:

- config adapters
- event hook registry
- AvoidHazards
- texto/font processor
- rearranjo de GUI sem overlap
- aiming/rotations
- cached block spheres
- graph search
- movable region scanner
- timed pickup tracker
- root domain/name generator
- request handler
- geometria e math
- stronghold estimator/generator
- text builder

Tambem ha recursos de teste para InventoryCleaner e Scaffold.

Recomendacao para FDP:

- Criar `src/test` real para partes sem dependencia direta do jogo.
- Priorizar testes de InventoryCleaner, rotacoes, scaffold placement, math, path/search e serializacao de config.
- Extrair logica pura de modulos grandes para classes testaveis.
- Antes de mexer em Scaffold, InventoryCleaner, RotationUtils ou Velocity, adicionar fixtures de comportamento atual.

## Funcionalidades recomendadas para portar

### Prioridade alta

| Item | Por que vale | Risco | Como portar |
| --- | --- | --- | --- |
| InventoryCleaner por facetas | Melhora qualidade de limpeza, ranking e manutencao | Medio | Reimplementar para itens 1.8, ignorando shield/crossbow/mace/offhand |
| Command builder | Melhora autocomplete, subcomandos e manutencao | Baixo/medio | Criar camada nova preservando comandos atuais |
| Test harness | Reduz regressao em modulos sensiveis | Baixo | Comecar por utilitarios puros |
| ToggleableValueGroup/ModeValueGroup | Organiza settings complexos | Medio | Adaptar ao sistema de values do FDP |
| DebugRecorder/PacketLogger | Facilita debug sem tocar gameplay | Baixo | Integrar com PacketDebugger existente |
| NoSlow Grim2364-1.8 | Modo explicitamente 1.8 | Medio/alto | Reescrever usando pacotes MCP 1.8 |
| Scaffold Prediction/SpeedLimiter | Pode melhorar scaffold atual sem troca total | Alto | Implementar atras de settings novos e testar |

### Prioridade media

| Item | Por que vale | Observacao |
| --- | --- | --- |
| AutoQueue | Util para servidores com filas/minigames | Depende de padroes de chat/servidor |
| AutoShop | Utilitario de servidor | Implementar por profiles |
| ReportHelper | Conveniencia | Sem automacao agressiva |
| GUICloser | Simples e util | Baixo risco |
| ItemScroller | UX de inventario | Adaptar a GUI 1.8 |
| MiddleClickAction | FDP tem MidClick, mas pode melhorar acoes | Mesclar conceitos |
| TextFieldProtect | Evita vazamento em campos sensiveis | Baixo risco |
| TargetLock | Util para combate | Precisa respeitar Target/Teams atuais |
| AutoDodge | Ideia interessante | Depende de predicao de projetil |
| AutoLeave | Simples | Deve ter confirmacoes e thresholds |
| EasyPearl/AutoPearl | Util em PvP | Adaptar para 1.8 pearl |
| Crosshair | Visual isolado | Baixo risco |
| CameraClip | Visual/movimento de camera | Testar com FreeCam/FreeLook |
| BlockOutline | Visual isolado | Baixo risco |
| HitFX | Visual isolado | Integrar ao CombatVisuals |
| ItemTags | Visual util | Cuidado com performance |
| LogoffSpot | Util para PvP | Baixo/medio |
| Radar | FDP pode ganhar radar separado | Integrar HUD |
| VoidESP | Visual util | Baixo risco |
| Zoom | Simples e esperado | Baixo risco |
| StrongholdFinder | Diferencial | Precisa matematica 1.8 e testes |

### Prioridade baixa ou condicional

| Item | Motivo |
| --- | --- |
| Svelte/MCEF theme | Migra stack inteira de UI e aumenta risco |
| ViaFabricPlus | FDP ja e 1.8.9 nativo |
| GraalVM script engine | Custo alto em Java 8 |
| Marketplace | Depende de backend |
| Cosmetics/cloud | Depende de infra |
| Elytra modules | Nao se aplica a 1.8.9 |
| Modern item modules | Nao se aplica a 1.8.9 |

## Funcionalidades que nao devem ser portadas

Nao recomendo portar:

- ServerCrasher.
- Exploits de sign/translation.
- Paper completion/window crashers.
- Invalid position crashers.
- Console spammer.
- Dupe.
- Modos cujo objetivo principal seja derrubar, travar ou degradar servidor.

Motivo: alem de serem instaveis e alto risco para o projeto, nao melhoram a qualidade do cliente 1.8.9. Se o FDP mantiver qualquer modulo desse tipo, ele deve ficar isolado, sem defaults ativos, sem payloads novos copiados e sem prioridade de manutencao.

## Roadmap sugerido

### Fase 1: infraestrutura segura

1. Criar testes basicos para utilitarios puros.
2. Criar fixtures de inventario e rotacao.
3. Implementar command builder novo sem remover comandos antigos.
4. Adicionar grupos de valores opcionais.
5. Melhorar PacketDebugger usando ideias de PacketLogger/DebugRecorder.

### Fase 2: modulos de baixo risco

1. Crosshair.
2. Zoom.
3. BlockOutline.
4. LogoffSpot.
5. GUICloser.
6. TextFieldProtect.
7. ItemTags.
8. BetterTab/BetterTitle, se encaixar na UI atual.

### Fase 3: gameplay utilitario

1. InventoryCleaner por facetas.
2. ChestCleaner, se nao conflitar com ChestStealer.
3. AutoQueue.
4. AutoShop por profiles.
5. EasyPearl/AutoPearl.
6. AutoDodge.
7. StrongholdFinder.

### Fase 4: modos sensiveis

1. NoSlow Grim2364-1.8.
2. Fly Vulcan286-18 como modo experimental.
3. Scaffold Prediction/SpeedLimiter/SprintControl.
4. Disabler SwingOrder/NoAction, se fizer sentido.
5. Speed variantes Grim/Sentinel/Vulcan apenas se houver ganho real sobre os modos FDP.

## Observacoes de licenca

LiquidBounce nextgen e FDPClient sao projetos GPL. Se codigo for copiado ou derivado diretamente, deve-se manter compatibilidade de licenca, creditos e distribuicao de fonte conforme GPL. Mesmo assim, por causa das diferencas tecnicas, a recomendacao pratica e reimplementar em cima da base FDP, citando a inspiracao quando uma logica vier diretamente do nextgen.

## Conclusao

O maior valor do LiquidBounce nextgen para o FDPClient 1.8.9 nao esta em copiar modulos de combate/movimento em massa. O FDP ja e forte em 1.8 nesses pontos. O maior ganho esta em engenharia: inventario mais inteligente, comandos melhores, configs mais organizadas, testes, diagnostico, UI/UX mais clara e alguns modulos utilitarios/visuais isolados.

Para gameplay sensivel, o caminho correto e comparar modo por modo e portar apenas o que tiver ganho real para 1.8.9. Para exploits destrutivos, a recomendacao e nao portar.
