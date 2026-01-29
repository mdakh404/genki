![Genki Logo](https://raw.githubusercontent.com/mdakh404/genki/refs/heads/main/src/main/resources/genki/img/genki_logo.png)

# Genki - Desktop messaging app

Genki is a desktop messaging application for real-time communication. Built with JavaFX and MongoDB, and managed using Maven, it supports instant messaging, friend requests, group creation, and group chats.


---

## Genki Demo

[![Product Name Screen Shot][product-screenshot]](https://example.com)

Here's a blank template to get started. To avoid retyping too much info, do a search and replace with your text editor for the following:

`github_username`, `repo_name`, `twitter_handle`, `linkedin_username`, `email_client`, `email`, `project_title`, `project_description`, `project_license`

[Back to top](#project_title)

---

### Built With

#### [![Java](https://skillicons.dev/icons?i=java&theme=light)](https://skillicons.dev)

#### [![Eclipse](https://skillicons.dev/icons?i=eclipse&theme=light)](https://skillicons.dev)

#### [![Git](https://skillicons.dev/icons?i=git&theme=light)](https://skillicons.dev)

#### [![Github](https://skillicons.dev/icons?i=github&theme=light)](https://skillicons.dev)

#### [![Maven](https://skillicons.dev/icons?i=maven&theme=light)](https://skillicons.dev)

#### [![MongoDB](https://skillicons.dev/icons?i=mongodb&theme=light)](https://skillicons.dev)


---

## Getting Started

This is an example of how you may give instructions on setting up your project locally.

### Prerequisites

- JDK 21+
- Maven 3.8+
- Cloudinary API_KEY, SECRET and CLOUD_NAME
- MongoDB credentials
- Eclipse IDE 

### Installation

To install and run Genki locally:

1. Clone the repository:

```sh
user@machine:~$ git clone https://github.com/mdakh404/genki.git
user@machine:~$ cd genki
```

2. Install project dependencies:

```sh
user@machine:~/genki$ mvn clean install
```

3. Edit your .env file with your Cloudinary API Key:

```
CLOUDINARY_URL=cloudinary://<API_KEY>:<API_SECRET>:@<CLOUD_NAME>
```



4. Add your MongoDB URI connection string in the `src/main/java/genki/utils/DBConnection.java` file:

```
private final String connectionURI = "mongodb+srv://<USER>:<PASS>@<ATLAS_CLUSTER_URL>/?appName=Genki";
```

5. Run the application:

```sh
user@machine:~/genki$ mvn javafx:run
```


## Features

- 🔐 Robust authentication system (password hashing using Bcrypt)

- 👥 Creating & joining groups (public and private)

- 🤝 Sending and accepting friend requests

- 🧑 Profile personalization (bio & profile picture)

- 💬 Private and group messaging


## Contributing

Your contributions are **greatly appreciated**.

To contribute to this project, please follow the following steps:

1. Fork the Project
2. Create your Feature Branch `git checkout -b feature`
3. Commit your Changes `git commit -m 'Add my-feature'`
4. Push to the Branch `git push origin feature`
5. Open a Pull Request


## License

Genki is open-source and available under the [MIT License](https://github.com/mdakh404/genki/blob/main/LICENSE).


## Contact

- Email: akhrazmoad14@gmail.com
