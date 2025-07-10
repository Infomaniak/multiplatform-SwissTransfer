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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.0/STCore.xcframework.zip",
            checksum: "bdd48b843c7948aad35df9b2505b8a9f46ebc1608f470c4c84620de2bcf89616"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.0/STDatabase.xcframework.zip",
            checksum: "4cbd5470d36923ed59e49e1be61abadd64185943a88c55636cb346bcaaabbff7"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.0/STNetwork.xcframework.zip",
            checksum: "2695dc8fc02a27e546e93680c890f5150807ffee804428d97ed089c6191a9079"
        ),
    ]
)
