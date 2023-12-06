# SAE_3.03_messagerie - Un système de messagerie instantanée en Java

Notre SAE vous permet de discuter sur une application client-serveur de messagerie instantanée, avec des fonctionnalités de groupe de discussion(channel), où les utilisateurs peuvent s’échanger des messages textuels en temps réel. 

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
6. Créer ou rejoindre des groupes de discussion pour échanger des messages.


javac -cp ..\jar\json-20230618.jar *.java