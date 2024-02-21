# SAE_3.03_messagerie - Un système de messagerie instantanée en Java

## Introduction
    La présente SAÉ a pour objectif le développement d'une application client-serveur d'un réseau social. Les utilisateurs auront la possibilité de publier des messages, de suivre d'autres utilisateurs, et d'interagir au travers de différentes fonctionnalités.


## Objectifs

- Implémenter un client permettant aux utilisateurs de se connecter au serveur.
- Permettre au client d'afficher les messages des personnes suivies.
- Permettre au client de poster des publications.

- Développer un serveur capable de recevoir, traiter et réexpédier les messages aux utilisateurs appropriés.

## Architecture de l'application

### Le client
    Le client se connecte au serveur en fournissant l'adresse IP ou le nom du serveur, ainsi que son nom d'utilisateur. Il peut consulter les messages de personnes qu'il suit et publier ses propres messages. Les fonctionnalités incluent :

-   Afficher les messages des utilisateurs suivis dans l'ordre chronologique avec une limite de messages affichés.

-   Créer un compte utilisateur s'il n'est pas enregistré sur le serveur.

- Poster des messages visibles par ses abonnés.
-   Commander des actions telles que suivre (/follow), se désabonner (/unfollow), aimer un message (/like), supprimer un message (/delete).

### Le serveur

    Le serveur accepte les connexions de nouveaux clients. Il doit traiter plusieurs messages en parallèle. Les fonctionnalités incluent :

-   Recevoir et réexpédier les messages aux utilisateurs appropriés.

-   Commander des actions en ligne de commande comme supprimer un message (/delete) ou supprimer un utilisateur et ses messages (/remove).

-   Utiliser le format JSON pour les échanges de messages.

## Prérequis
- Java 8 ou version ultérieure

## Installation
1. Cloner le projet depuis GitHub

2. Compiler le projet en utilisant la commande `javac -cp ./org.json.jar *.java` dans le répertoire du projet

## Utilisation

### Lancement du serveur

1. Lancer le serveur en utilisant la commande `java ` <----- ICI

2. Saisir l'ip du serveur (localhost si vous le faite en local)

4. Saisir le port sur lequel tout les client vont se connecter (un nombre à quatre chiffre).

### Lancement du serveur 

1. Lancer le client en utilisant la commande `java ` <----- ICI

2. Saisir le port sur lequel le servaur a été lancer précédemant.

### Création de compte

1. Une fois le serveur client lancée vous pouvez mettre un nom qui sera le pseudo et le mots de passe pour vous connecter ulterieurement.

2. Une fois connecter vous pouvez écrire des message dans le salon.

### Commande du client
En gros tu met tt ce quil y a dans le /help  <----- ICI

### Commande du serveur 
la meme  <----- ICI


## Explication des classes 

### Classe `Client`


Rapport de Travail - Projet de Reseau social Client-Serveur

Nom de la Personne : Blandeau Erwan
Poste : Chef de projet

Introduction :

Au cours de la période spécifiée, J'ai a joué un rôle essentiel dans le développement et la mise en œuvre du projet de communication client-serveur. Le projet visait à établir une plateforme permettant aux utilisateurs de se connecter à un serveur, d'échanger des messages et de participer à des salons de discussion.

Responsabilités Principales :

    Développement du Client Java : J'ai a été responsable de la conception et du développement du client Java. Cela inclut la mise en place de la logique de connexion au serveur, la gestion des noms d'utilisateur, et l'établissement des sockets pour la communication bidirectionnelle.

    Gestion des Utilisateurs : J'ai mis en place un système de gestion des utilisateurs, y compris la vérification des noms d'utilisateur et des mots de passe, ainsi que l'ajout d'utilisateurs au fichier JSON.

    Interface Utilisateur : J'ai a contribué à la mise en place d'une interface utilisateur simple pour la saisie des informations nécessaires, y compris le nom d'utilisateur et le mot de passe.

    Communication Client-Serveur : J'ai été responsable de la mise en place du mécanisme de communication entre le client et le serveur, y compris la gestion des messages entrants et sortants.

Résultats Obtenus :

    Le client développé par J'ai s'est avéré stable et fonctionnel, permettant une connexion réussie au serveur et la participation aux salons de discussion.

    Le système de gestion des utilisateurs a contribué à assurer l'unicité des noms d'utilisateur et à renforcer la sécurité avec la vérification des mots de passe.

    L'interface utilisateur intuitive a facilité l'expérience des utilisateurs lors de la saisie des informations nécessaires à la connexion.

Défis Rencontrés :

    J'ai a fait face au défi de la gestion des erreurs de connexion, en particulier lorsqu'un nom d'utilisateur était déjà utilisé. La mise en place d'une logique de gestion des erreurs a été cruciale pour améliorer l'expérience utilisateur.

Conclusion :

En conclusion, J'ai a joué un rôle clé dans le succès du projet de communication client-serveur. Son expertise dans le développement Java, la gestion des utilisateurs et la communication client-serveur a contribué de manière significative à la réalisation d'une plateforme fonctionnelle et fiable.