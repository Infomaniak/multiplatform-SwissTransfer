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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STCore.xcframework.zip",
            checksum: "21ec263bff7369f2d9166a69dda11453f3f55ef2e90de4fecdd743df610b1c09"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STDatabase.xcframework.zip",
            checksum: "863307d18d985f10ce9e93a6fa4633427a165bc1e2124e49f40fab9bec656057"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.1/STNetwork.xcframework.zip",
            checksum: "04aa13818363e825bdd9704b2de30514fcba7f89dde7ff4edaa49d95d6df4bc1"
        ),
    ]
)
