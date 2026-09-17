-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: aguiaexpress
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','cria-t-tipos-perfis','SQL','V1__cria-t-tipos-perfis.sql',2115077835,'root','2026-05-14 16:20:13',59,1),(2,'2','cria-t-usuarios','SQL','V2__cria-t-usuarios.sql',1494893026,'root','2026-05-15 14:32:08',76,1),(3,'3','cria-t-categorias','SQL','V3__cria-t-categorias.sql',32370904,'root','2026-05-15 16:29:23',67,1),(4,'4','cria-t-status-ideia','SQL','V4__cria-t-status-ideia.sql',729093733,'root','2026-05-15 17:08:37',97,1),(5,'5','cria-t-orientacoes-estrategicas','SQL','V5__cria-t-orientacoes-estrategicas.sql',853212838,'root','2026-05-15 17:41:34',60,1),(6,'6','cria-t-ideias','SQL','V6__cria-t-ideias.sql',27815037,'root','2026-05-16 00:36:44',117,1),(7,'7','cria-t-status-projeto','SQL','V7__cria-t-status-projeto.sql',830652036,'root','2026-05-16 01:21:04',85,1),(8,'8','cria-t-projetos','SQL','V8__cria-t-projetos.sql',-432751885,'root','2026-05-16 14:14:32',92,1),(9,'9','cria-t-badges','SQL','V9__cria-t-badges.sql',1004666641,'root','2026-05-16 15:29:00',90,1),(10,'10','cria-t-usuario-badges','SQL','V10__cria-t-usuario-badges.sql',-357832649,'root','2026-05-16 18:48:52',90,1),(11,'11','cria-t-votos-ideia','SQL','V11__cria-t-votos-ideia.sql',1158940033,'root','2026-05-16 19:44:33',77,1),(12,'12','make idea priority decimal','SQL','V12__make_idea_priority_decimal.sql',408596838,'root','2026-09-12 23:48:29',206,1),(13,'13','backfill operational milestone badges','SQL','V13__backfill_operational_milestone_badges.sql',520178743,'root','2026-09-15 19:05:40',61,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_badges`
--

DROP TABLE IF EXISTS `t_badges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_badges` (
  `id_badge` bigint NOT NULL AUTO_INCREMENT,
  `nm_badge` varchar(80) NOT NULL,
  `ds_descricao` varchar(255) DEFAULT NULL,
  `ds_criterio` varchar(255) DEFAULT NULL,
  `ds_icone` varchar(100) DEFAULT NULL,
  `st_status` int DEFAULT '1',
  PRIMARY KEY (`id_badge`),
  UNIQUE KEY `uk_badges_nome` (`nm_badge`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_badges`
--

LOCK TABLES `t_badges` WRITE;
/*!40000 ALTER TABLE `t_badges` DISABLE KEYS */;
INSERT INTO `t_badges` VALUES (1,'PRIMEIRA_IDEIA','Concedida ao usuário que cadastrou sua primeira ideia.','Cadastrar pelo menos uma ideia no sistema.','primeira_ideia',1),(2,'IDEIA_APROVADA','Concedida ao usuário que teve uma ideia aprovada.','Ter pelo menos uma ideia com status APROVADA.','ideia_aprovada',1),(3,'TOP_COLABORADOR','Concedida ao usuário com alta participação no envio de ideias.','Cadastrar múltiplas ideias ou alcançar alta pontuação.','top_colaborador',1),(4,'PROJETO_IMPLEMENTADO','Concedida quando uma ideia do usuário é convertida em projeto.','Ter uma ideia convertida em projeto.','projeto_implementado',1),(5,'MESTRE_DA_INOVACAO','Concedida ao usuário com impacto relevante na geração de ideias.','Acumular pontos, votos ou ideias aprovadas de forma consistente.','mestre_inovacao',1);
/*!40000 ALTER TABLE `t_badges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_categorias`
--

DROP TABLE IF EXISTS `t_categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_categorias` (
  `id_categoria` bigint NOT NULL AUTO_INCREMENT,
  `nm_categoria` varchar(80) NOT NULL,
  `ds_descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`),
  UNIQUE KEY `uk_categorias_nome` (`nm_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_categorias`
--

LOCK TABLES `t_categorias` WRITE;
/*!40000 ALTER TABLE `t_categorias` DISABLE KEYS */;
INSERT INTO `t_categorias` VALUES (2,'OPERACOES','Ideias relacionadas à melhoria das operações internas'),(3,'SUSTENTABILIDADE','Ideias relacionadas à redução de impacto ambiental e uso eficiente de recursos'),(4,'ATENDIMENTO','Ideias relacionadas à melhoria da experiência do cliente'),(5,'TECNOLOGIA','Ideias relacionadas à automação, sistemas e soluções digitais'),(6,'SEGURANCA','Ideias relacionadas à segurança operacional, do usuário ou dos colaboradores'),(7,'PRODUTIVIDADE','Ideias relacionadas à eficiência, redução de retrabalho e aumento de produtividade'),(8,'LOGISTICA_OPERACIONAL','Ideias relacionadas à otimização de rotas, entregas, frota e processos logísticos');
/*!40000 ALTER TABLE `t_categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ideias`
--

DROP TABLE IF EXISTS `t_ideias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ideias` (
  `id_ideia` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `id_categoria` bigint NOT NULL,
  `id_status_ideia` bigint NOT NULL,
  `id_orientacao` bigint NOT NULL,
  `ds_titulo` varchar(150) NOT NULL,
  `ds_descricao` varchar(1000) NOT NULL,
  `ds_problema` varchar(1000) DEFAULT NULL,
  `ds_solucao_proposta` varchar(1000) DEFAULT NULL,
  `nr_impacto` int DEFAULT '0',
  `nr_viabilidade` int DEFAULT '0',
  `nr_prioridade` decimal(4,2) NOT NULL DEFAULT '0.00',
  `nr_total_votos` int DEFAULT '0',
  `dt_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `dt_data_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_ideia`),
  KEY `fk_ideias_usuario` (`id_usuario`),
  KEY `fk_ideias_categoria` (`id_categoria`),
  KEY `fk_ideias_status_ideia` (`id_status_ideia`),
  KEY `fk_ideias_orientacao` (`id_orientacao`),
  CONSTRAINT `fk_ideias_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `t_categorias` (`id_categoria`),
  CONSTRAINT `fk_ideias_orientacao` FOREIGN KEY (`id_orientacao`) REFERENCES `t_orientacoes_estrategicas` (`id_orientacao`),
  CONSTRAINT `fk_ideias_status_ideia` FOREIGN KEY (`id_status_ideia`) REFERENCES `t_status_ideia` (`id_status_ideia`),
  CONSTRAINT `fk_ideias_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `t_usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ideias`
--

LOCK TABLES `t_ideias` WRITE;
/*!40000 ALTER TABLE `t_ideias` DISABLE KEYS */;
INSERT INTO `t_ideias` VALUES (1,3,2,5,1,'Teste de ideia para virar projeto com IAv2','Ideia criada apenas para testar a criação de projeto com autenticação.','Precisamos validar o fluxo de projeto.','Criar uma ideia nova e depois convertê-la em projeto.',8,7,3.00,3,'2026-05-24 20:39:56','2026-09-10 16:08:23'),(2,3,2,4,1,'Teste de ideia para virar projeto com IA','Ideia criada apenas para testar a criação de projeto com autenticação.','Precisamos validar o fluxo de projeto.','Criar uma ideia nova e depois convertê-la em projeto.',0,0,1.00,1,'2026-05-24 22:43:51','2026-09-10 15:35:21'),(5,8,2,2,2,'Acelerar Rotas com IA','Reduzir tempo entre o trajeto A e B','Otimizar o uso de combustivel','Reduzir o peso das mercadorias e bagagens em 30%, para otimizar a velocidade do veiculo',0,0,1.89,2,'2026-09-14 17:08:57','2026-09-15 16:27:07'),(6,8,8,4,2,'Otimizar consumo de combustiveis','Observando como reduzir a emissao de poluentes','Trocar veiculos a diesel pelo modelo hibrido','Trocar pelo menos 10% da rota que faz o trajeto entre A e B.',0,0,3.16,4,'2026-09-15 15:17:55','2026-09-16 19:48:00');
/*!40000 ALTER TABLE `t_ideias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_orientacoes_estrategicas`
--

DROP TABLE IF EXISTS `t_orientacoes_estrategicas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_orientacoes_estrategicas` (
  `id_orientacao` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `ds_titulo` varchar(150) NOT NULL,
  `ds_descricao` varchar(1000) NOT NULL,
  `nm_area_estrategica` varchar(100) DEFAULT NULL,
  `st_status` int DEFAULT '1',
  `dt_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `dt_data_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_orientacao`),
  KEY `fk_orientacoes_usuario` (`id_usuario`),
  CONSTRAINT `fk_orientacoes_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `t_usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_orientacoes_estrategicas`
--

LOCK TABLES `t_orientacoes_estrategicas` WRITE;
/*!40000 ALTER TABLE `t_orientacoes_estrategicas` DISABLE KEYS */;
INSERT INTO `t_orientacoes_estrategicas` VALUES (1,1,'Redução inteligente de custos operacionais sem IA','Priorizar ideias que reduzam desperdícios, otimizem rotas, melhorem uso de recursos e gerem economia mensurável para a operação.','Operações',0,'2026-05-24 20:15:48','2026-05-24 23:37:12'),(2,1,'Expansão da cultura de inovação ','Estimular ideias que melhorem processos internos, reduzam custos e aumentem o engajamento dos colaboradores.','Inovação',1,'2026-05-24 22:11:41','2026-05-25 15:59:08'),(3,1,'Expansão da cultura de inovação com IA','Estimular ideias que melhorem processos internos, reduzam custos e aumentem o engajamento dos colaboradores.','Inovação',1,'2026-05-24 23:14:10','2026-05-24 23:14:10'),(4,7,'Expansão da cultura de inovação com Agents','Estimular ideias que melhorem processos internos, reduzam custos e aumentem o engajamento dos colaboradores.','Inovação',1,'2026-09-10 15:21:31','2026-09-10 15:21:31');
/*!40000 ALTER TABLE `t_orientacoes_estrategicas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_projetos`
--

DROP TABLE IF EXISTS `t_projetos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_projetos` (
  `id_projeto` bigint NOT NULL AUTO_INCREMENT,
  `id_ideia` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_status_projeto` bigint NOT NULL,
  `ds_titulo` varchar(150) NOT NULL,
  `ds_descricao` varchar(1000) DEFAULT NULL,
  `ds_objetivo` varchar(1000) DEFAULT NULL,
  `vl_investimento` decimal(12,2) DEFAULT '0.00',
  `vl_roi` decimal(10,2) DEFAULT '0.00',
  `vl_retorno_financeiro` decimal(12,2) DEFAULT '0.00',
  `vl_reducao_custos` decimal(12,2) DEFAULT '0.00',
  `vl_ganho_produtividade` decimal(10,2) DEFAULT '0.00',
  `dt_inicio` date DEFAULT NULL,
  `dt_previsao_fim` date DEFAULT NULL,
  `dt_conclusao` date DEFAULT NULL,
  `dt_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `dt_data_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_projeto`),
  UNIQUE KEY `uk_projetos_ideia` (`id_ideia`),
  KEY `fk_projetos_usuario` (`id_usuario`),
  KEY `fk_projetos_status_projeto` (`id_status_projeto`),
  CONSTRAINT `fk_projetos_ideia` FOREIGN KEY (`id_ideia`) REFERENCES `t_ideias` (`id_ideia`),
  CONSTRAINT `fk_projetos_status_projeto` FOREIGN KEY (`id_status_projeto`) REFERENCES `t_status_projeto` (`id_status_projeto`),
  CONSTRAINT `fk_projetos_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `t_usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_projetos`
--

LOCK TABLES `t_projetos` WRITE;
/*!40000 ALTER TABLE `t_projetos` DISABLE KEYS */;
INSERT INTO `t_projetos` VALUES (2,1,2,3,'Projeto teste com JWT com Figma','Projeto criado para validar permissão de gestor.','Confirmar que apenas gestor consegue criar projeto.',10000.00,12.50,25000.00,5000.00,10.00,'2026-06-01','2026-08-30','2026-09-10','2026-05-24 23:07:13','2026-09-10 15:40:49');
/*!40000 ALTER TABLE `t_projetos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_status_ideia`
--

DROP TABLE IF EXISTS `t_status_ideia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_status_ideia` (
  `id_status_ideia` bigint NOT NULL AUTO_INCREMENT,
  `nm_status` varchar(50) NOT NULL,
  `ds_descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_status_ideia`),
  UNIQUE KEY `uk_status_ideia_nome` (`nm_status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_status_ideia`
--

LOCK TABLES `t_status_ideia` WRITE;
/*!40000 ALTER TABLE `t_status_ideia` DISABLE KEYS */;
INSERT INTO `t_status_ideia` VALUES (1,'NOVA','Ideia cadastrada e aguardando análise inicial'),(2,'EM_ANALISE','Ideia em análise por gestores'),(3,'PRIORIZADA','Ideia priorizada para avaliação ou execução futura'),(4,'APROVADA','Ideia aprovada para virar projeto ou iniciativa'),(5,'REJEITADA','Ideia rejeitada após análise'),(6,'CONVERTIDA_EM_PROJETO','Ideia aprovada e convertida em projeto');
/*!40000 ALTER TABLE `t_status_ideia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_status_projeto`
--

DROP TABLE IF EXISTS `t_status_projeto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_status_projeto` (
  `id_status_projeto` bigint NOT NULL AUTO_INCREMENT,
  `nm_status` varchar(50) NOT NULL,
  `ds_descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_status_projeto`),
  UNIQUE KEY `uk_status_projeto_nome` (`nm_status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_status_projeto`
--

LOCK TABLES `t_status_projeto` WRITE;
/*!40000 ALTER TABLE `t_status_projeto` DISABLE KEYS */;
INSERT INTO `t_status_projeto` VALUES (1,'PLANEJADO','Projeto criado, mas ainda não iniciado'),(2,'EM_ANDAMENTO','Projeto em execução'),(3,'CONCLUIDO','Projeto finalizado'),(4,'CANCELADO','Projeto interrompido ou cancelado');
/*!40000 ALTER TABLE `t_status_projeto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_tipos_perfis`
--

DROP TABLE IF EXISTS `t_tipos_perfis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_tipos_perfis` (
  `id_perfil` bigint NOT NULL AUTO_INCREMENT,
  `nm_perfil` varchar(50) NOT NULL,
  `ds_descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_perfil`),
  UNIQUE KEY `uk_tipos_perfis_nome` (`nm_perfil`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_tipos_perfis`
--

LOCK TABLES `t_tipos_perfis` WRITE;
/*!40000 ALTER TABLE `t_tipos_perfis` DISABLE KEYS */;
INSERT INTO `t_tipos_perfis` VALUES (1,'OPERACIONAL','Usuário operacional que cadastra e acompanha ideias'),(2,'GESTOR','Usuário gestor que prioriza ideias e acompanha projetos'),(3,'LIDER','Usuário líder que gerencia orientações estratégicas e visualiza resultados');
/*!40000 ALTER TABLE `t_tipos_perfis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_usuario_badges`
--

DROP TABLE IF EXISTS `t_usuario_badges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_usuario_badges` (
  `id_usuario_badge` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `id_badge` bigint NOT NULL,
  `dt_conquista` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario_badge`),
  UNIQUE KEY `uk_usuario_badges_usuario_badge` (`id_usuario`,`id_badge`),
  KEY `fk_usuario_badges_badge` (`id_badge`),
  CONSTRAINT `fk_usuario_badges_badge` FOREIGN KEY (`id_badge`) REFERENCES `t_badges` (`id_badge`),
  CONSTRAINT `fk_usuario_badges_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `t_usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_usuario_badges`
--

LOCK TABLES `t_usuario_badges` WRITE;
/*!40000 ALTER TABLE `t_usuario_badges` DISABLE KEYS */;
INSERT INTO `t_usuario_badges` VALUES (1,3,1,'2026-09-15 16:05:39'),(2,3,2,'2026-09-15 16:05:39'),(3,8,1,'2026-09-15 16:05:39'),(4,8,2,'2026-09-15 16:05:39');
/*!40000 ALTER TABLE `t_usuario_badges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_usuarios`
--

DROP TABLE IF EXISTS `t_usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_usuarios` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `id_perfil` bigint NOT NULL,
  `nm_nome` varchar(100) NOT NULL,
  `ds_email` varchar(150) NOT NULL,
  `ds_senha` varchar(255) NOT NULL,
  `ds_cargo` varchar(100) DEFAULT NULL,
  `ds_departamento` varchar(100) DEFAULT NULL,
  `nr_pontos` int DEFAULT '0',
  `st_status` int DEFAULT '1',
  `dt_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `dt_data_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uk_usuarios_email` (`ds_email`),
  KEY `fk_usuarios_perfil` (`id_perfil`),
  CONSTRAINT `fk_usuarios_perfil` FOREIGN KEY (`id_perfil`) REFERENCES `t_tipos_perfis` (`id_perfil`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_usuarios`
--

LOCK TABLES `t_usuarios` WRITE;
/*!40000 ALTER TABLE `t_usuarios` DISABLE KEYS */;
INSERT INTO `t_usuarios` VALUES (1,3,'Mariana Souza','mariana.souza@aguiaexpress.com','$2a$10$P6HA.PQjP1Ay9kRN7BBm9OLYSJT52ePrTMQaFXYjTOGEUb6Vogvo6','Diretora de Estratégia','Estratégia',0,1,'2026-05-24 18:28:08','2026-05-24 18:28:08'),(2,2,'Carlos Mendes','carlos.mendes@aguiaexpress.com','$2a$10$3sxdPfagB6MB5wekgQ6zM.nYind.qnr3c1egSduoIwkKsQuMl3Hyi','Gerente de Operações','Operações',0,1,'2026-05-24 18:28:25','2026-05-24 18:28:25'),(3,1,'Ana Martins','ana.martins@aguiaexpress.com','$2a$10$gY0uOohK0KFKA/1D01t0Te5i7Fx4NeyRaLYy3LjscVwRLhmXKCsyS','Operadora de Atendimento','Atendimento',0,1,'2026-05-24 18:28:37','2026-05-24 18:28:37'),(5,2,'test','test@test.com','$2a$10$88XaqPF0gz.UjSw6D0zWoeihYOjeilXTGoFlUSDD2C32XknoHXXqe','Operadora de Atendimento','Atendimento',0,1,'2026-09-08 16:37:35','2026-09-08 16:37:35'),(6,3,'Nome do Líder','lider@example.com','123456','Líder','Inovação',0,1,'2026-09-10 15:10:18','2026-09-10 15:12:24'),(7,3,'Líder Postman','lider.postman@local.test','$2y$10$T8QQxdDUgvjfv.B.V/yas.sGm3oe6qtW3TOE4EMhpdT//3Uke71w2','Líder','Testes',0,1,'2026-09-10 15:16:17','2026-09-10 15:16:17'),(8,1,'Joao','test2@test.com','$2a$10$0NcqvACzSAgaPw8esT/E8eY2b0cnuMhQOydy44x90TPrdnrjZwHPW','Operadora de Atendimento','Atendimento',0,1,'2026-09-10 15:22:10','2026-09-10 15:22:10');
/*!40000 ALTER TABLE `t_usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_votos_ideia`
--

DROP TABLE IF EXISTS `t_votos_ideia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_votos_ideia` (
  `id_voto` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `id_ideia` bigint NOT NULL,
  `nr_valor` int DEFAULT '1',
  `dt_voto` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_voto`),
  UNIQUE KEY `uk_votos_ideia_usuario_ideia` (`id_usuario`,`id_ideia`),
  KEY `fk_votos_ideia_ideia` (`id_ideia`),
  CONSTRAINT `fk_votos_ideia_ideia` FOREIGN KEY (`id_ideia`) REFERENCES `t_ideias` (`id_ideia`),
  CONSTRAINT `fk_votos_ideia_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `t_usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_votos_ideia`
--

LOCK TABLES `t_votos_ideia` WRITE;
/*!40000 ALTER TABLE `t_votos_ideia` DISABLE KEYS */;
INSERT INTO `t_votos_ideia` VALUES (1,3,1,1,'2026-05-24 21:54:21'),(4,1,1,1,'2026-05-25 15:56:19'),(5,1,2,1,'2026-05-25 15:59:24'),(6,2,1,1,'2026-05-25 16:04:25'),(7,8,5,1,'2026-09-14 17:09:39'),(8,8,6,1,'2026-09-15 15:18:03'),(9,5,6,1,'2026-09-15 15:21:45'),(10,7,6,1,'2026-09-15 16:27:04'),(11,7,5,1,'2026-09-15 16:27:06'),(12,2,6,1,'2026-09-16 19:37:47');
/*!40000 ALTER TABLE `t_votos_ideia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'aguiaexpress'
--

--
-- Dumping routines for database 'aguiaexpress'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-16 21:56:43
