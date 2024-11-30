# Micronaut Demo Project

This is a demo project created to explore the features and capabilities of the Micronaut Framework.

The primary goal of this project was to gain hands-on experience by building a simple API for creating and retrieving users. In the process, I implemented common features, such as a global API error handler, query specifications for dynamic queries, soft delete functionality, profiles with custom configuration per environment, and both unit and integration tests.

## Run the demo
Init the database:
```bash
 docker compose up --build -d
```
Set environment variables as per `env.example`:
```
MICRONAUT_ENVIRONMENTS=local
```
Run the project:
```bash
./gradlew run
```
## Test the API
### Create a User
```http
POST http://localhost:8080/users
```
Body:
```json
{
    "name": "John Doe",
    "email": "jdoe@gmail.com",
    "type": "T1",
    "gender": "MALE",
    "birthdate": "1995-11-18"
}
```
### Get User by ID
```http
GET http://localhost:8080/users/{id}
```
### Search Users
```http
GET http://localhost:8080/users
```
Query params:
| Param         | Example           | Description                                            |
|---------------|-------------------|--------------------------------------------------------|
| `name`          | `J`                 | Filters users by name, matching the given string.      |
| `types`         | `T1,T3`             | Filters users by type(s), comma-separated.             |
| `gender`        | `MALE`              | Filters users by gender.                               |
| `older_than`    | `21`                | Filters users older than the specified age.            |
| `size`          | `10`                | Specifies the number of users to return per page.      |
| `page`          | `0`                 | Specifies the page number for paginated results.       |
| `sort`          | `name`              | Sorts the results by the specified field.              |
