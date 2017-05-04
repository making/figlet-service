# figlet-service

```
  _____   _           _          _   
 |  ___| (_)   __ _  | |   ___  | |_ 
 | |_    | |  / _` | | |  / _ \ | __|
 |  _|   | | | (_| | | | |  __/ | |_ 
 |_|     |_|  \__, | |_|  \___|  \__|
              |___/                  
```

[Figlet](https://en.wikipedia.org/wiki/FIGlet) as a Cloud Foundry Route Service

## Deploy to Pivotal Web Services

```
./mvnw clean package -DskipTests=true
cf push
```

## Create Route Service

```
cf create-user-provided-service figlet-service -r https://figlet-service.cfapps.io
```

## Bind Route Service

```
cf bind-route-service cfapps.io figlet-service --hostname <your-subdomain>
```