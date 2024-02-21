# SAE_3.03_Système et Réseau - Réseau social Tuit’o

Le but de cette SAÉ est de développer une application client-serveur d’un réseau social : les utilisateurs peuvent publier des messages, et suivre d’autres utilisateurs pour consulter leurs publications.

## Auteurs
- Blandeau erwan
- Lucidor Léo

## Fonctionnalités
- &#9745; Connexion au serveur
- &#9745; Envoi de message
- &#9745; Réception de message
- &#9744; /follow <nom utilisateur> permet de s’abonner à un nouvel utilisateur. Si celui-ci
n’existe pas un message d’erreur s’affiche
- &#9744; /unfollow <nom_utilisateur> se désabonner
- &#9744; /like <id_message> : aime un message
- &#9744; /delete <id_message> : supprime un de ses messages

## Prérequis
- Java 8 ou version ultérieure

## Installation
1. Cloner le projet depuis GitHub
2. Compiler le projet en utilisant la commande `javac *.java` dans le répertoire du projet
3. Lancer le serveur en utilisant la commande `java ExecutableServeur`
4. Lancer le client en utilisant la commande `java ExecutableClient`

## Utilisation
1. Lancer le serveur en utilisant la commande `java ExecutableServeur`
2. Lancer le client en utilisant la commande `java ExecutableClient`
3. Saisir l'ip du serveur (localhost si vous le faite en local)
4. Saisir le port (1234 si ça n'a pas été changé)
5. Saisir un pseudo pour vous identifier auprès des autres utilisateurs
6. Ecrivez pour pour échanger des messages.

## Fonctionnement
Le serveur est lancé en premier, il attend la connexion des clients. Une fois un client connecté, il peut envoyer des messages au serveur qui les redistribuera aux autres clients connectés.

## Tests

- &#9745; Connexion au serveur
- &#9745; Envoi de message
- &#9745; Réception de message
- &#9744; /follow <nom utilisateur> permet de s’abonner à un nouvel utilisateur. Si celui-ci
n’existe pas un message d’erreur s’affiche
- &#9744; /unfollow <nom_utilisateur> se désabonner
- &#9744; /like <id_message> : aime un message
- &#9744; /delete <id_message> : supprime un de ses messages


## Architecture

UML Actuelle du projet : [UML](./uml/Diagramme_actuelle.png)

UML du projet avec les fonctionnalités à venir : [UML](./uml/Diagramme_future.png)

## Améliorations possibles
- &#9744; /follow <nom utilisateur> permet de s’abonner à un nouvel utilisateur. Si celui-ci n’existe pas un message d’erreur s’affiche

- &#9744; /unfollow <nom_utilisateur> se désabonner si l'utilisateur n'existe pas un message d'erreur s'affiche

- &#9744; /like <id_message> : aime un message si le message n'existe pas un message d'erreur s'affiche

- &#9744; /delete <id_message> : supprime un de ses messages si le message n'existe pas un message d'erreur s'affiche

- &#9744; /list : affiche la liste des utilisateurs connectés au serveur (en plus de la liste des messages)

- &#9744; /list <nom_utilisateur> : affiche la liste des messages d’un utilisateur


## Répartition des tâches
- Blandeau erwan : Serveur
- Lucidor Léo : Client

## Difficultés rencontrées
- Blandeau erwan : le serveur ne recevait pas les messages des clients
- Lucidor Léo : le client ne ce connectait pas au serveur et ne recevait pas les messages des autres clients

## Conclusion
Nous avons réussi à faire fonctionner le serveur et le client, ce qui etait donc demandé dans le premier rendu. Nous avons pu tester les fonctionnalités de base du serveur et du client. Nous avons rencontré des difficultés au début du projet mais nous avons réussi à les surmonter. Nous avons pu mettre en place une architecture qui nous permettra d'ajouter des fonctionnalités supplémentaires par la suite.


