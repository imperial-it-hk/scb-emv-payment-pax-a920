# <center>commonlib<center>

## Function
    Store public dependency modules
## Usage
* 1.Add module dependencies
```
api project(':commonlib')
```

* 2.Call the code of the module

## Precautions
    Since commonlib currently includes an aar package, you need to add the following configuration to the same level as the android node.
    ```
    repositories {
        flatDir {
            dirs '../commonlib/libs', 'libs'
        }
    }
    ```