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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.2/STCore.xcframework.zip",
            checksum: "bcdddd66ed0872b45ae07559596b3417ca7dbe63a75139a585af964aa8a4b769"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.2/STDatabase.xcframework.zip",
            checksum: "fe4d5ae19437ee27809c9e8dcb17205e4db7c27daa193d94026592f78259212b"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.2/STNetwork.xcframework.zip",
            checksum: "e039595b5e5526a834e5b8ed57e31fafc58afeda17b7081d97af75041196a7c2"
        ),
    ]
)
