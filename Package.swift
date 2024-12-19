// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.1/STCore.xcframework.zip",
            checksum: "8d44d292f282a0cf1abf76ccd31b3ccfc632b1c2125fcd77e2eeabdaa6c2e1a2"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.1/STDatabase.xcframework.zip",
            checksum: "a3bbf273d82f5e96dda34dee64e5947452ecf6192bc316bd97f07c0c152a135b"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.1/STNetwork.xcframework.zip",
            checksum: "27ec3a950903e103695aeeb1653e67b8dfebbb1fddfd63de97e42aa14819aaaa"
        ),
    ]
)
