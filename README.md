# Motivora

Un mot peut changer votre journée

Motivora est une API backend développée en Spring Boot dont l’objectif est de fournir des motivations et citations consultables par thème ou auteur. Elle propose des fonctionnalités de consultation publique et de gestion administrative des contenus, avec une architecture modulable prête à évoluer.

---

## Table des matières

1. Présentation
2. Fonctionnalités
3. Architecture
4. Installation et configuration
5. Endpoints principaux
6. Tests
7. Contribution
8. Licence

---

## 1. Présentation

Motivora est une API REST conçue pour gérer une base de citations et de motivations, leurs auteurs, les thèmes associés ainsi que des opérations de recherche et de filtrage.

L’objectif est de proposer une plateforme simple, fiable et extensible, pouvant évoluer vers des fonctionnalités avancées comme la contribution utilisateur, la modération ou des recommandations personnalisées.

---

## 2. Fonctionnalités

### Public

- Récupérer toutes les citations publiées (pagination)
- Récupérer une citation par identifiant ou par slug
- Rechercher des citations par mot-clé
- Lister des citations par auteur
- Obtenir une citation aléatoire

### Administrateur

- Créer, modifier, activer et désactiver des citations
- Gérer les auteurs
- Gérer les thèmes
- Activer ou désactiver des citations par auteur ou par thème

---

## 3. Architecture

Motivora repose sur une architecture claire et maintenable, inspirée des bonnes pratiques Spring Boot.

- Controller : exposition des endpoints REST
- Service : logique métier et règles fonctionnelles
- Repository : accès aux données via Spring Data JPA

Les responsabilités sont strictement séparées afin de garantir la lisibilité, la testabilité et l’évolutivité du projet.

---

## 4. Installation et configuration

### Pré-requis

- Java 17 ou supérieur
- Maven
- Une base de données relationnelle (PostgreSQL recommandé)

### Cloner le projet

bash git clone https://github.com/NAJPRO/Motivora.git cd Motivora 

### Configuration

Configurer la base de données dans le fichier application.properties :

properties spring.datasource.url=jdbc:postgresql://localhost:5432/motivora spring.datasource.username=your_username spring.datasource.password=your_password spring.jpa.hibernate.ddl-auto=update 

### Lancer l’application

bash ./mvnw spring-boot:run 

L’API sera accessible par défaut sur http://localhost:8080.

---

## 5. Endpoints principaux

### Citations (public)

- GET /quotes : liste paginée des citations publiées
- GET /quotes/{idOrSlug} : détail d’une citation
- GET /quotes/author/{authorId} : citations publiées par auteur
- GET /quotes/search?keyword=... : recherche par mot-clé
- GET /quotes/random : citation aléatoire

### Citations (admin)

- POST /admin/quotes : créer une citation
- PUT /admin/quotes/{idOrSlug} : modifier une citation
- PATCH /admin/quotes/{idOrSlug}/enable : activer une citation
- PATCH /admin/quotes/{idOrSlug}/disable : désactiver une citation

Les endpoints administratifs sont destinés à être protégés par un système d’authentification et de rôles.

---

## 6. Tests

Le projet inclut des tests unitaires et peut être étendu avec des tests d’intégration pour les endpoints REST.

Pour lancer les tests :

bash ./mvnw test 

---

## 7. Contribution

Les contributions sont les bienvenues.

Vous pouvez proposer des améliorations, signaler des bugs ou suggérer de nouvelles fonctionnalités via les issues du dépôt GitHub.

---

## 8. Licence

Motivora est un projet open-source distribué sous licence MIT.